/* SnipLink frontend — talks to the backend over same-origin /api. No dependencies. */
(function () {
  'use strict';

  var API = '/api/v1/urls';
  var HISTORY_KEY = 'sniplink_history_v1';
  var MAX_HISTORY = 12;

  // ---------- helpers ----------
  function $(id) { return document.getElementById(id); }

  var toastEl = $('toast');
  var toastTimer = null;
  function toast(message, kind) {
    toastEl.textContent = message;
    toastEl.className = 'toast show' + (kind ? ' ' + kind : '');
    toastEl.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () {
      toastEl.className = 'toast';
      toastEl.hidden = true;
    }, 3200);
  }

  function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.querySelector('.btn-label').hidden = loading;
    btn.querySelector('.spinner').hidden = !loading;
  }

  function showError(inputId, errorId, message) {
    var input = $(inputId);
    var err = $(errorId);
    if (message) {
      err.textContent = message;
      err.hidden = false;
      if (input) input.setAttribute('aria-invalid', 'true');
    } else {
      err.hidden = true;
      if (input) input.removeAttribute('aria-invalid');
    }
  }

  function friendlyError(status, body, fallback) {
    if (status === 400) return body && body.message ? body.message : 'Please check the values you entered.';
    if (status === 404) return 'That link was not found or has expired.';
    if (status === 409) return (body && body.message) || 'That alias is already taken — try another one.';
    if (status === 429) return 'Too many requests — please wait a minute and try again.';
    if (status >= 500) return 'The server had a problem. Please try again in a moment.';
    return fallback || 'Something went wrong. Please try again.';
  }

  function request(path, options) {
    return fetch(API + path, options).then(function (res) {
      var ct = res.headers.get('content-type') || '';
      var parse = ct.indexOf('application/json') !== -1 ? res.json() : res.text();
      return Promise.resolve(parse).then(function (body) {
        if (!res.ok) {
          var err = new Error(friendlyError(res.status, body));
          err.status = res.status;
          throw err;
        }
        return body;
      });
    }).catch(function (err) {
      if (err && err.status) throw err;
      throw new Error('Could not reach the server. Is the backend running?');
    });
  }

  // ---------- history (localStorage) ----------
  function loadHistory() {
    try {
      return JSON.parse(localStorage.getItem(HISTORY_KEY)) || [];
    } catch (e) {
      return [];
    }
  }
  function saveHistory(items) {
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(items.slice(0, MAX_HISTORY)));
    } catch (e) { /* storage unavailable — history just won't persist */ }
  }

  var historyList = $('history-list');
  var historyEmpty = $('history-empty');
  var clearBtn = $('clear-history');
  var sessionCount = $('stat-shortened');

  function renderHistory() {
    var items = loadHistory();
    historyList.innerHTML = '';
    historyEmpty.hidden = items.length > 0;
    clearBtn.hidden = items.length === 0;
    sessionCount.textContent = String(items.length);
    items.forEach(function (item) {
      var li = document.createElement('li');
      li.className = 'history-item';

      var code = document.createElement('span');
      code.className = 'h-code';
      code.textContent = '/' + item.code;

      var url = document.createElement('span');
      url.className = 'h-url';
      url.textContent = item.longUrl;
      url.title = item.longUrl;

      var clicks = document.createElement('span');
      clicks.className = 'h-clicks';
      clicks.textContent = (item.clicks == null ? '—' : item.clicks) + ' clicks';

      var open = document.createElement('a');
      open.className = 'icon-btn';
      open.href = item.shortUrl;
      open.target = '_blank';
      open.rel = 'noopener';
      open.textContent = 'Open';

      var copy = document.createElement('button');
      copy.className = 'icon-btn';
      copy.type = 'button';
      copy.textContent = 'Copy';
      copy.setAttribute('aria-label', 'Copy ' + item.shortUrl);
      copy.addEventListener('click', function () { copyText(item.shortUrl); });

      var del = document.createElement('button');
      del.className = 'icon-btn danger';
      del.type = 'button';
      del.textContent = 'Remove';
      del.setAttribute('aria-label', 'Remove ' + item.code + ' from history');
      del.addEventListener('click', function () {
        saveHistory(loadHistory().filter(function (h) { return h.code !== item.code; }));
        renderHistory();
      });

      li.append(code, url, clicks, open, copy, del);
      historyList.appendChild(li);
    });
  }

  function addToHistory(entry) {
    var items = loadHistory().filter(function (h) { return h.code !== entry.code; });
    items.unshift(entry);
    saveHistory(items);
    renderHistory();
  }

  function formatExpiry(seconds) {
    if (seconds % 86400 === 0) {
      var days = seconds / 86400;
      return 'Expires in ' + days + (days === 1 ? ' day' : ' days');
    }
    var hours = Math.round(seconds / 3600);
    return 'Expires in ' + hours + (hours === 1 ? ' hour' : ' hours');
  }

  function copyText(text) {
    function done() { toast('Copied to clipboard', 'success'); }
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(done, function () { fallbackCopy(text, done); });
    } else {
      fallbackCopy(text, done);
    }
  }
  function fallbackCopy(text, done) {
    var ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.select();
    try {
      document.execCommand('copy');
      done();
    } catch (e) {
      toast('Copy failed — select the link manually', 'error');
    }
    document.body.removeChild(ta);
  }

  // ---------- shorten form ----------
  var form = $('shorten-form');
  var urlInput = $('long-url');
  var aliasInput = $('alias');
  var expirySelect = $('expiry');
  var shortenBtn = $('shorten-btn');
  var result = $('result');
  var resultLink = $('result-link');
  var resultMeta = $('result-meta');

  form.addEventListener('submit', function (e) {
    e.preventDefault();
    showError('long-url', 'url-error', null);
    showError('alias', 'alias-error', null);

    var url = urlInput.value.trim();
    var alias = aliasInput.value.trim();

    if (!url) {
      showError('long-url', 'url-error', 'Please paste the URL you want to shorten.');
      urlInput.focus();
      return;
    }
    if (!/^(https?|ftp):\/\//i.test(url)) {
      showError('long-url', 'url-error', 'The URL must start with http://, https:// or ftp://');
      urlInput.focus();
      return;
    }
    if (alias && !/^[a-zA-Z0-9_-]{3,20}$/.test(alias)) {
      showError('alias', 'alias-error', 'Use 3–20 characters: letters, numbers, _ or -');
      aliasInput.focus();
      return;
    }

    var payload = { url: url, expirationSeconds: parseInt(expirySelect.value, 10) };
    if (alias) payload.customAlias = alias;

    setLoading(shortenBtn, true);
    request('', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    }).then(function (data) {
      result.hidden = false;
      resultLink.href = data.shortUrl;
      resultLink.textContent = data.shortUrl.replace(/^https?:\/\//, '');
      resultLink.setAttribute('aria-label', 'Open short link ' + data.shortUrl);
      resultMeta.textContent = 'Code: ' + data.code + ' · ' + formatExpiry(payload.expirationSeconds);
      addToHistory({ code: data.code, shortUrl: data.shortUrl, longUrl: url, clicks: 0 });
      toast('Short link created', 'success');
      result.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }).catch(function (err) {
      if (err.status === 409) {
        showError('alias', 'alias-error', err.message);
        aliasInput.focus();
      } else if (err.status === 400) {
        showError('long-url', 'url-error', err.message);
      } else {
        toast(err.message, 'error');
      }
    }).finally(function () {
      setLoading(shortenBtn, false);
    });
  });

  $('copy-btn').addEventListener('click', function () { copyText(resultLink.href); });
  $('track-btn').addEventListener('click', function () {
    var code = resultLink.href.substring(resultLink.href.lastIndexOf('/') + 1);
    $('lookup-code').value = code;
    lookup(code);
    document.getElementById('analytics').scrollIntoView({ behavior: 'smooth' });
  });

  // ---------- stats lookup ----------
  var lookupForm = $('lookup-form');
  var lookupInput = $('lookup-code');
  var lookupBtn = $('lookup-btn');
  var statsCard = $('stats-card');

  function lookup(rawCode) {
    showError('lookup-code', 'lookup-error', null);
    var code = (rawCode || '').trim().replace(/^.*\//, '').split('?')[0].split('#')[0];
    if (!code) {
      showError('lookup-code', 'lookup-error', 'Enter a short code first.');
      lookupInput.focus();
      return;
    }
    setLoading(lookupBtn, true);
    request('/' + encodeURIComponent(code) + '/stats')
      .then(function (data) {
        statsCard.hidden = false;
        var link = $('stats-link');
        link.href = data.shortUrl;
        link.textContent = data.shortUrl.replace(/^https?:\/\//, '');
        $('stats-clicks').textContent = data.clickCount;
        var items = loadHistory().map(function (h) {
          if (h.code === data.code) h.clicks = data.clickCount;
          return h;
        });
        saveHistory(items);
        renderHistory();
      })
      .catch(function (err) {
        statsCard.hidden = true;
        showError('lookup-code', 'lookup-error', err.message);
      })
      .finally(function () { setLoading(lookupBtn, false); });
  }

  lookupForm.addEventListener('submit', function (e) {
    e.preventDefault();
    lookup(lookupInput.value);
  });

  // ---------- misc wiring ----------
  clearBtn.addEventListener('click', function () {
    saveHistory([]);
    renderHistory();
    toast('History cleared');
  });

  // scroll reveal (transform/opacity only, disabled under reduced motion)
  if ('IntersectionObserver' in window &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches === false) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) {
          en.target.classList.add('visible');
          io.unobserve(en.target);
        }
      });
    }, { threshold: 0.12 });
    document.querySelectorAll('.feature-card, .stats-card, .shorten-card, .lookup-card, .code-block')
      .forEach(function (el) {
        el.classList.add('reveal');
        io.observe(el);
      });
  }

  renderHistory();
})();
