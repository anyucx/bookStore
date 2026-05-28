import { onMounted, onUnmounted } from 'vue';
import router from '@/router';

let enabled = true;
let started = false;
let lastHiddenTs = 0;
let visibleCount = 0;
const MIN_VISIBLE_INTERVAL = 500;

function defendWindowApis() {
  const hidden = () => document.hidden;

  const origFocus = window.focus.bind(window);
  const origOpen = window.open.bind(window);
  const origAlert = window.alert.bind(window);
  const origConfirm = window.confirm.bind(window);
  const origPrompt = window.prompt.bind(window);
  const origPrint = window.print.bind(window);
  const origRequestFs = Element.prototype.requestFullscreen.bind(document.documentElement);
  const origWebkitRequestFs = (Element.prototype as any).webkitRequestFullscreen?.bind(document.documentElement);

  try {
    Object.defineProperty(window, 'focus', {
      configurable: false, writable: false,
      value: () => { if (!hidden()) origFocus(); },
    });
  } catch { info('guard_focus_fail'); }

  try {
    window.alert = (...args: any[]) => { if (!hidden()) origAlert(...args); };
    window.confirm = (...args: any[]) => { if (hidden()) return false; return origConfirm(...args); };
    window.prompt = (...args: any[]) => { if (hidden()) return null; return origPrompt(...args); };
    window.print = () => { if (!hidden()) origPrint(); };
  } catch { info('guard_dialog_fail'); }

  try {
    (window as any).open = (...args: any[]) => { if (hidden()) return null; return origOpen(...args); };
  } catch { info('guard_open_fail'); }

  try {
    Element.prototype.requestFullscreen = function () {
      if (hidden()) return Promise.reject(new Error('blocked'));
      return origRequestFs.call(this);
    };
    if (origWebkitRequestFs) {
      (Element.prototype as any).webkitRequestFullscreen = function () {
        if (hidden()) return;
        origWebkitRequestFs.call(this);
      };
    }
  } catch { info('guard_fullscreen_fail'); }
}

type LogLevel = 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';

function inIframe() { return window !== window.top; }

function getBrowserInfo() {
  return {
    userAgent: navigator.userAgent,
    platform: navigator.platform,
    language: navigator.language,
    screenWidth: window.screen.width,
    screenHeight: window.screen.height,
    windowWidth: window.innerWidth,
    windowHeight: window.innerHeight,
    documentHidden: document.hidden,
    hasFocus: document.hasFocus(),
    inIframe: inIframe(),
    hasOpener: !!window.opener,
    referrer: document.referrer || '(none)',
  };
}

function guardAgainstExtension() {
  if (inIframe()) {
    info('blocked_in_iframe', getBrowserInfo());
  }
  defendWindowApis();
}

function currentRoute() {
  try { return router.currentRoute.value?.fullPath || 'unknown'; } catch { return 'unknown'; }
}

function send(level: LogLevel, message: string, data?: unknown) {
  if (!enabled) return false;
  const body = JSON.stringify({
    level,
    message,
    data: data !== undefined ? data : undefined,
    timestamp: Date.now(),
  });
  try {
    navigator.sendBeacon('/api/logs/frontend', new Blob([body], { type: 'application/json' }));
  } catch {
    // sendBeacon failed silently
  }
  return true;
}

function info(message: string, data?: unknown) { send('INFO', message, data); }
function warn(message: string, data?: unknown) { send('WARN', message, data); }
function error(message: string, data?: unknown) { send('ERROR', message, data); }

function trackPageLifecycle() {
  info('page_mount', Object.assign(getBrowserInfo(), { route: currentRoute() }));

  let hiddenNotify = true;
  document.addEventListener('visibilitychange', () => {
    const ts = Date.now();
    if (document.hidden) {
      lastHiddenTs = ts;
      if (hiddenNotify) {
        info('page_hidden', { ts, route: currentRoute() });
        hiddenNotify = false;
      }
    } else {
      visibleCount++;
      const elapsed = lastHiddenTs ? ts - lastHiddenTs : 0;
      const suspicious = lastHiddenTs > 0 && elapsed < MIN_VISIBLE_INTERVAL;
      info('page_visible', {
        ts, hadFocus: document.hasFocus(), sinceHidden: elapsed, visibleCount,
        route: currentRoute(), suspicious: suspicious || undefined,
        minInterval: suspicious ? MIN_VISIBLE_INTERVAL : undefined,
      });
      if (!suspicious) hiddenNotify = true;
    }
  });

  window.addEventListener('focus', () => {
    info('window_focus', { ts: Date.now(), route: currentRoute() });
  });

  window.addEventListener('blur', () => {
    info('window_blur', { ts: Date.now(), route: currentRoute() });
  });

  window.addEventListener('popstate', () => {
    info('popstate', { url: location.href, route: currentRoute(), ts: Date.now() });
  });
}

export function startLogging() {
  if (started) {
    info('startLogging_duplicate_call', Object.assign(getBrowserInfo(), { route: currentRoute() }));
    return;
  }
  started = true;
  guardAgainstExtension();
  info('logging_started', Object.assign(getBrowserInfo(), { route: currentRoute() }));
  trackPageLifecycle();
}

export function stopLogging() {
  enabled = false;
}

export function usePageLogging(pageName: string) {
  onMounted(() => info('page_enter', { page: pageName }));
  onUnmounted(() => info('page_leave', { page: pageName }));
}

const logger = { info, warn, error, startLogging, stopLogging, usePageLogging };
export default logger;
