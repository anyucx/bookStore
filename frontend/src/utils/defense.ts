const TRACKING_PARAMS = ['__callback__', 'callback', '__amp_source_origin'];

function hasTrackingParams(url: string | URL | null): boolean {
  if (!url) return false;
  try {
    const u = typeof url === 'string' ? new URL(url, location.origin) : url;
    return TRACKING_PARAMS.some(p => u.searchParams.has(p));
  } catch {
    return false;
  }
}

function stripURL(url: string | URL | null): string | URL | null {
  if (!url || typeof url !== 'string') return url;
  try {
    const u = new URL(url, location.origin);
    if (!hasTrackingParams(u)) return url;
    TRACKING_PARAMS.forEach(p => u.searchParams.delete(p));
    return u.pathname + u.search + u.hash;
  } catch {
    return url;
  }
}

function cleanCurrentURL(): void {
  if (hasTrackingParams(location.href)) {
    const u = new URL(location.href);
    TRACKING_PARAMS.forEach(p => u.searchParams.delete(p));
    history.replaceState(null, '', u.pathname + u.search + u.hash);
  }
}

/* ─── Extension-injected element detection ─────────────────────────── */

const EXTENSION_URL_RE = /(?:chrome-extension|moz-extension|safari-web-extension|ms-browser-extension|extension):\/\//i;

const EXTENSION_SELECTOR =
  '[id^="cx_"],[id^="mx_"],[id^="trk_"],[id^="ext-"],' +
  '[data-extension],iframe[src*="extension"],iframe[src*="chrome-extension"],' +
  'iframe[src*="moz-extension"],iframe[src*="safari-web-extension"]';

function isExtensionNode(node: Node): boolean {
  if (node.nodeType === Node.ELEMENT_NODE) {
    const el = node as Element;

    // 1. Tag-based: extension-injected iframes with extension URLs
    if (el.tagName === 'IFRAME') {
      const src = el.getAttribute('src') || '';
      if (EXTENSION_URL_RE.test(src)) return true;
      // Blank iframes injected as tracking pixels
      if (!src && el.getAttribute('sandbox') !== null) return true;
    }

    // 2. ID/class patterns common to extensions (Grammarly, AdBlock, LastPass, etc.)
    const id = el.id || '';
    const cls = el.className?.toString?.() || '';
    if (/^(cx_|mx_|trk_|ext-|ext_|ntk_)/i.test(id)) return true;
    if (/\b(extension-toolbar|ext-toolbar|grammarly|1password|bitwarden-toolbar)\b/i.test(cls)) return true;

    // 3. Scripts/styles with extension URLs
    if (el.tagName === 'SCRIPT' || el.tagName === 'STYLE' || el.tagName === 'LINK') {
      const src = el.getAttribute('src') || el.getAttribute('href') || '';
      if (EXTENSION_URL_RE.test(src)) return true;
    }

    // 4. data-extension attribute marker
    if (el.hasAttribute('data-extension') || el.hasAttribute('data-ext-injected')) return true;

    // 5. Very small fixed overlays (extension badges) positioned outside #app
    const style = getComputedStyle(el);
    if (
      style.position === 'fixed' &&
      el.closest('#app') === null &&
      el.tagName !== 'BODY' &&
      el.tagName !== 'HTML'
    ) {
      const w = parseFloat(style.width) || 0;
      const h = parseFloat(style.height) || 0;
      // Badge-like overlays smaller than 80×80px in corners
      if (w > 0 && w < 80 && h > 0 && h < 80) return true;
    }
  }

  // Text nodes inside <head> that shouldn't be there
  if (node.nodeType === Node.TEXT_NODE && node.parentElement?.tagName === 'HEAD') {
    const text = node.textContent || '';
    if (text.includes('extension') || text.includes('inject')) return true;
  }

  return false;
}

function removeExtensionNodes(root: Node): void {
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT);
  const toRemove: Node[] = [];
  let n: Node | null;
  while ((n = walker.nextNode())) {
    if (isExtensionNode(n)) toRemove.push(n);
  }
  toRemove.forEach(n => n.parentNode?.removeChild(n));
}

/* ─── MutationObserver: auto-strip extension injections ─────────────── */

let _observerActive = true;

function startDOMGuard(): void {
  const target = document.documentElement;

  const mo = new MutationObserver(mutations => {
    if (!_observerActive) return;

    for (const m of mutations) {
      for (const added of m.addedNodes) {
        if (isExtensionNode(added)) {
          // Remove immediately — do not wait for next frame
          added.parentNode?.removeChild(added);
          continue;
        }
        // Also scan children (e.g. a <div> wrapper containing an extension <iframe>)
        if (added.nodeType === Node.ELEMENT_NODE) {
          removeExtensionNodes(added);
        }
      }

      // Re-parenting: check if attributes changed to inject extension markers
      if (m.type === 'attributes' && m.target.nodeType === Node.ELEMENT_NODE) {
        const el = m.target as Element;
        if (isExtensionNode(el)) {
          el.parentNode?.removeChild(el);
        }
      }
    }
  });

  mo.observe(target, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ['id', 'class', 'src', 'href', 'data-extension', 'data-ext-injected'],
  });
}

/* ─── DOM method interception: block extensions from appending ──────── */

function interceptDOMMethods(): void {
  const nativeAppendChild = Node.prototype.appendChild;
  const nativeInsertBefore = Node.prototype.insertBefore;
  const nativePrepend = Element.prototype.prepend;
  const nativeAppend = Element.prototype.append;
  const nativeSetAttribute = Element.prototype.setAttribute;

  const BLOCKED_TARGETS = new Set<Node>();

  function guardAppendChild(this: Node, child: Node): Node {
    if (BLOCKED_TARGETS.has(this) || isExtensionNode(child)) {
      return child as Node;
    }
    return nativeAppendChild.call(this, child) as Node;
  }

  function guardInsertBefore(this: Node, newChild: Node, refChild: Node | null): Node {
    if (BLOCKED_TARGETS.has(this) || isExtensionNode(newChild)) {
      return newChild as Node;
    }
    return nativeInsertBefore.call(this, newChild, refChild) as Node;
  }

  function guardPrepend(this: Element, ...nodes: (string | Node)[]): void {
    if (BLOCKED_TARGETS.has(this)) return;
    const real = nodes.filter(n => !(typeof n !== 'string' && isExtensionNode(n)));
    if (real.length) nativePrepend.apply(this, real as any);
  }

  function guardAppend(this: Element, ...nodes: (string | Node)[]): void {
    if (BLOCKED_TARGETS.has(this)) return;
    const real = nodes.filter(n => !(typeof n !== 'string' && isExtensionNode(n)));
    if (real.length) nativeAppend.apply(this, real as any);
  }

  // Block extensions from adding malicious attributes to our elements
  function guardSetAttribute(this: Element, name: string, value: string): void {
    if (EXTENSION_URL_RE.test(value)) return;
    nativeSetAttribute.call(this, name, value);
  }

  Node.prototype.appendChild = guardAppendChild as typeof Node.prototype.appendChild;
  Node.prototype.insertBefore = guardInsertBefore as typeof Node.prototype.insertBefore;
  Element.prototype.prepend = guardPrepend as typeof Element.prototype.prepend;
  Element.prototype.append = guardAppend as typeof Element.prototype.append;
  Element.prototype.setAttribute = guardSetAttribute as typeof Element.prototype.setAttribute;

  // Schedule blocking of <head> and <body> after they exist
  requestAnimationFrame(() => {
    if (document.head) BLOCKED_TARGETS.add(document.head);
    if (document.body) BLOCKED_TARGETS.add(document.body);
  });

  // Expose pause control for visibility handler
  (window as any).__defensePauseDOMGuard__ = (pause: boolean) => {
    _observerActive = !pause;
  };
}

/* ─── Visibility / blur anti-flicker ────────────────────────────────── */

function setupVisibilityGuard(): void {
  // Save original Element Plus transition values
  const html = document.documentElement;

  const disableTransitions = () => {
    html.style.setProperty('--el-transition-duration', '0s');
    html.style.setProperty('--el-transition-duration-fast', '0s');
    // Also clamp all CSS transitions globally
    html.style.setProperty('--app-transition', '0s');
  };

  const enableTransitions = () => {
    html.style.removeProperty('--el-transition-duration');
    html.style.removeProperty('--el-transition-duration-fast');
    html.style.removeProperty('--app-transition');
    // Force synchronous repaint
    html.offsetHeight; // eslint-disable-line @typescript-eslint/no-unused-expressions
  };

  // visibilitychange (tab hidden / minimized)
  let savedCSS = '';
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      savedCSS = html.style.cssText;
      disableTransitions();
      // Pause DOM observer to avoid processing extension-triggered mutations while hidden
      (window as any).__defensePauseDOMGuard__?.(true);
    } else {
      (window as any).__defensePauseDOMGuard__?.(false);
      html.style.cssText = savedCSS; // restore previous inline styles
      enableTransitions();
      // Clean any extension nodes injected while we were hidden
      removeExtensionNodes(document.documentElement);
    }
  });

  // window blur (user Alt+Tab, minimizes window)
  let _blurCount = 0;
  window.addEventListener('blur', () => {
    _blurCount++;
    disableTransitions();
    (window as any).__defensePauseDOMGuard__?.(true);
  }, { passive: true });

  window.addEventListener('focus', () => {
    _blurCount = Math.max(0, _blurCount - 1);
    if (_blurCount === 0) {
      (window as any).__defensePauseDOMGuard__?.(false);
      enableTransitions();
      removeExtensionNodes(document.documentElement);
    }
  }, { passive: true });

  // Also catch the page being backgrounded via the hidden state
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      // Suppress all error toasts while hidden
      html.style.setProperty('pointer-events', 'none');
    } else {
      html.style.removeProperty('pointer-events');
    }
  });
}

/* ─── Original URL tracking protection ──────────────────────────────── */

export function protectFromExtensions(): void {
  cleanCurrentURL();

  const nativePushState = (() => {
    let proto: any = history;
    while (proto) {
      const desc = Object.getOwnPropertyDescriptor(proto, 'pushState');
      if (desc && typeof desc.value === 'function') return desc.value.bind(history);
      proto = Object.getPrototypeOf(proto);
    }
    return history.pushState.bind(history);
  })();

  const nativeReplaceState = (() => {
    let proto: any = history;
    while (proto) {
      const desc = Object.getOwnPropertyDescriptor(proto, 'replaceState');
      if (desc && typeof desc.value === 'function') return desc.value.bind(history);
      proto = Object.getPrototypeOf(proto);
    }
    return history.replaceState.bind(history);
  })();

  let _wrapping = false;

  history.pushState = function (data: unknown, _unused: string, url?: string | URL | null) {
    if (_wrapping) return nativePushState(data, _unused, url);
    _wrapping = true;
    try {
      return nativePushState(data, _unused, stripURL(url ?? null));
    } finally {
      _wrapping = false;
    }
  };

  history.replaceState = function (data: unknown, _unused: string, url?: string | URL | null) {
    if (_wrapping) return nativeReplaceState(data, _unused, url);
    _wrapping = true;
    try {
      return nativeReplaceState(data, _unused, stripURL(url ?? null));
    } finally {
      _wrapping = false;
    }
  };

  let _cleaning = false;
  window.addEventListener('popstate', () => {
    if (_cleaning) return;
    if (hasTrackingParams(location.href)) {
      _cleaning = true;
      try {
        const u = new URL(location.href);
        TRACKING_PARAMS.forEach(p => u.searchParams.delete(p));
        nativeReplaceState(null, '', u.pathname + u.search + u.hash);
      } finally {
        _cleaning = false;
      }
    }
  });

  // ── New: DOM defense ──
  if (typeof MutationObserver !== 'undefined') {
    startDOMGuard();
  }
  interceptDOMMethods();
  setupVisibilityGuard();
}
