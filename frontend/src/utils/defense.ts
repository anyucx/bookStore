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

export function protectFromExtensions(): void {
  cleanCurrentURL();

  const origPushState = history.pushState.bind(history);
  const origReplaceState = history.replaceState.bind(history);

  history.pushState = function (data: unknown, _unused: string, url?: string | URL | null) {
    return origPushState(data, _unused, stripURL(url ?? null));
  };
  history.replaceState = function (data: unknown, _unused: string, url?: string | URL | null) {
    return origReplaceState(data, _unused, stripURL(url ?? null));
  };

  window.addEventListener('popstate', () => {
    if (hasTrackingParams(location.href)) {
      const u = new URL(location.href);
      TRACKING_PARAMS.forEach(p => u.searchParams.delete(p));
      history.replaceState(null, '', u.pathname + u.search + u.hash);
    }
  });
}
