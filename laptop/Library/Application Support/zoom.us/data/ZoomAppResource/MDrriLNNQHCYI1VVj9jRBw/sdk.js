var zoomSdk = (function () {
  'use strict';
  var t,
    n,
    e = function (t, n) {
      return (e =
        Object.setPrototypeOf ||
        ({
          __proto__: [],
        } instanceof Array &&
          function (t, n) {
            t.__proto__ = n;
          }) ||
        function (t, n) {
          for (var e in n) Object.prototype.hasOwnProperty.call(n, e) && (t[e] = n[e]);
        })(t, n);
    };
  function o(t, n, e, o) {
    return new (e || (e = Promise))(function (i, r) {
      function s(t) {
        try {
          u(o.next(t));
        } catch (t) {
          r(t);
        }
      }
      function a(t) {
        try {
          u(o.throw(t));
        } catch (t) {
          r(t);
        }
      }
      function u(t) {
        var n;
        t.done
          ? i(t.value)
          : ((n = t.value),
            n instanceof e
              ? n
              : new e(function (t) {
                  t(n);
                })).then(s, a);
      }
      u((o = o.apply(t, n || [])).next());
    });
  }
  function i(t, n) {
    var e,
      o,
      i,
      r,
      s = {
        label: 0,
        sent: function () {
          if (1 & i[0]) throw i[1];
          return i[1];
        },
        trys: [],
        ops: [],
      };
    return (
      (r = {
        next: a(0),
        throw: a(1),
        return: a(2),
      }),
      'function' == typeof Symbol &&
        (r[Symbol.iterator] = function () {
          return this;
        }),
      r
    );
    function a(r) {
      return function (a) {
        return (function (r) {
          if (e) throw new TypeError('Generator is already executing.');
          for (; s; )
            try {
              if (
                ((e = 1),
                o &&
                  (i =
                    2 & r[0]
                      ? o.return
                      : r[0]
                        ? o.throw || ((i = o.return) && i.call(o), 0)
                        : o.next) &&
                  !(i = i.call(o, r[1])).done)
              )
                return i;
              switch (((o = 0), i && (r = [2 & r[0], i.value]), r[0])) {
                case 0:
                case 1:
                  i = r;
                  break;
                case 4:
                  return (
                    s.label++,
                    {
                      value: r[1],
                      done: !1,
                    }
                  );
                case 5:
                  s.label++, (o = r[1]), (r = [0]);
                  continue;
                case 7:
                  (r = s.ops.pop()), s.trys.pop();
                  continue;
                default:
                  if (
                    !((i = s.trys),
                    (i = i.length > 0 && i[i.length - 1]) || (6 !== r[0] && 2 !== r[0]))
                  ) {
                    s = 0;
                    continue;
                  }
                  if (3 === r[0] && (!i || (r[1] > i[0] && r[1] < i[3]))) {
                    s.label = r[1];
                    break;
                  }
                  if (6 === r[0] && s.label < i[1]) {
                    (s.label = i[1]), (i = r);
                    break;
                  }
                  if (i && s.label < i[2]) {
                    (s.label = i[2]), s.ops.push(r);
                    break;
                  }
                  i[2] && s.ops.pop(), s.trys.pop();
                  continue;
              }
              r = n.call(t, s);
            } catch (t) {
              (r = [6, t]), (o = 0);
            } finally {
              e = i = 0;
            }
          if (5 & r[0]) throw r[1];
          return {
            value: r[0] ? r[1] : void 0,
            done: !0,
          };
        })([r, a]);
      };
    }
  }
  !(function (t) {
    (t.CONFIG = 'config'),
      (t.SET_VIRTUAL_BACKGROUND = 'setVirtualBackground'),
      (t.REMOVE_VIRTUAL_BACKGROUND = 'removeVirtualBackground'),
      (t.GET_SUPPORTED_JS_APIS = 'getSupportedJsApis'),
      (t.GET_MEETING_CONTEXT = 'getMeetingContext'),
      (t.GET_RUNNING_CONTEXT = 'getRunningContext'),
      (t.GET_APP_CONTEXT = 'getAppContext'),
      (t.OPEN_URL = 'openUrlInSysBrowser'),
      (t.GET_ACTIVE_MEMBER = 'zmGetActiveMembers'),
      (t.COPY_TO_CLIPBOARD = 'zmWriteTextToClipboard'),
      (t.GET_PAAP_SERVICE_INFO = 'zmGetPAAPLogServiceInfo'),
      (t.SHOW_NOTIFICATION = 'showNotification');
  })(t || (t = {})),
    (function (t) {
      (t.SHARE_APP = 'shareApp'), (t.SEND_APP_INVITATION = 'sendAppInvitation');
    })(n || (n = {}));
  var r = {},
    s = {},
    a = (function (t) {
      function n(e, o) {
        var i = t.call(this, e) || this;
        return (i.code = o), Object.setPrototypeOf(i, n.prototype), i;
      }
      return (
        (function (t, n) {
          function o() {
            this.constructor = t;
          }
          e(t, n),
            (t.prototype = null === n ? Object.create(n) : ((o.prototype = n.prototype), new o()));
        })(n, t),
        n
      );
    })(Error),
    u = (function () {
      function e(t) {
        (this.postMessage = t.postMessage), (this.version = t.version);
      }
      return (
        (e.prototype.native2js = function (t) {
          if ('apiResponse' === t.data.type) {
            var n = t.data.data.jsCallId;
            r[n](t.data.data);
          } else 'event' === t.data.type && s[t.data.name] && s[t.data.name](t.data.data);
        }),
        (e.prototype.callZoomApi = function (t, n, e, s) {
          return o(this, void 0, void 0, function () {
            var o,
              u,
              p = this;
            return i(this, function (i) {
              return (
                (o = 'id' + Math.random().toString(16).slice(2)),
                (u = {
                  jsCallId: o,
                  apiName: t,
                }),
                n && (u.data = n),
                [
                  2,
                  new Promise(function (t, n) {
                    var i = setTimeout(function () {
                      var t = new Error(
                        'The native client did not provide a response to the API call',
                      );
                      n(t), c(o);
                    }, e || 1e4);
                    !(function (t, n) {
                      r[t] = n;
                    })(o, function (e) {
                      var r = e.errorCode,
                        u = e.errorMessage,
                        p = e.result;
                      if (r || u) {
                        var l = new a(u, r);
                        n(l);
                      } else t(s ? s(p) : p);
                      c(o), clearTimeout(i);
                    }),
                      p.postMessage(u);
                  }),
                ]
              );
            });
          });
        }),
        (e.prototype.config = function (n) {
          return (
            void 0 === n && (n = {}),
            o(this, void 0, void 0, function () {
              return i(this, function (e) {
                return (
                  n.capabilities &&
                    ((n.js_api_lists = (function () {
                      for (var t = 0, n = 0, e = arguments.length; n < e; n++)
                        t += arguments[n].length;
                      var o = Array(t),
                        i = 0;
                      for (n = 0; n < e; n++)
                        for (var r = arguments[n], s = 0, a = r.length; s < a; s++, i++)
                          o[i] = r[s];
                      return o;
                    })(n.capabilities)),
                    delete n.capabilities),
                  (!n.js_api_lists || (n.js_api_lists && !Array.isArray(n.js_api_lists))) &&
                    (n.js_api_lists = []),
                  [2, this.callZoomApi(t.CONFIG, n)]
                );
              });
            })
          );
        }),
        (e.prototype.getSupportedJsApis = function () {
          return o(this, void 0, void 0, function () {
            return i(this, function (n) {
              return [
                2,
                this.callZoomApi(t.GET_SUPPORTED_JS_APIS, null, null, function (t) {
                  return p(t) ? JSON.parse(t) : t;
                }),
              ];
            });
          });
        }),
        (e.prototype.openUrl = function (n) {
          return o(this, void 0, void 0, function () {
            return i(this, function (e) {
              return new URL(n.url), [2, this.callZoomApi(t.OPEN_URL, n)];
            });
          });
        }),
        (e.prototype.getActiveMember = function (n) {
          return o(this, void 0, void 0, function () {
            return i(this, function (e) {
              return [2, this.callZoomApi(t.GET_ACTIVE_MEMBER, n)];
            });
          });
        }),
        (e.prototype.getAppContext = function () {
          return o(this, void 0, void 0, function () {
            return i(this, function () {
              return [2, this.callZoomApi(t.GET_APP_CONTEXT)];
            });
          });
        }),
        (e.prototype.getRunningContext = function (options) {
          return o(this, void 0, void 0, function () {
            return i(this, function () {
              return [2, this.callZoomApi(t.GET_RUNNING_CONTEXT, options)];
            });
          });
        }),
        (e.prototype.getTelemetryServiceInfo = function () {
          return o(this, void 0, void 0, function () {
            return i(this, function () {
              return [2, this.callZoomApi(t.GET_PAAP_SERVICE_INFO)];
            });
          });
        }),
        (e.prototype.getMeetingContext = function () {
          return o(this, void 0, void 0, function () {
            return i(this, function (n) {
              return [
                2,
                this.callZoomApi(t.GET_MEETING_CONTEXT, null, null, function (t) {
                  return p(t) ? JSON.parse(t) : t;
                }),
              ];
            });
          });
        }),
        (e.prototype.setVirtualBackground = function (n) {
          return o(this, void 0, void 0, function () {
            return i(this, function (e) {
              return new URL(n.fileUrl), [2, this.callZoomApi(t.SET_VIRTUAL_BACKGROUND, n, 12e4)];
            });
          });
        }),
        (e.prototype.copyToClipboard = function (options) {
          return o(this, void 0, void 0, function () {
            return i(this, function (e) {
              return [2, this.callZoomApi(t.COPY_TO_CLIPBOARD, options)];
            });
          });
        }),
        (e.prototype.showNotification = function (n) {
          return o(this, void 0, void 0, function () {
            return i(this, function (e) {
              return [2, this.callZoomApi(t.SHOW_NOTIFICATION, n)];
            });
          });
        }),
        (e.prototype.onShareApp = function (t) {
          this.addEventListener(n.SEND_APP_INVITATION, t);
        }),
        (e.prototype.onSendAppInvitation = function (t) {
          this.addEventListener(n.SEND_APP_INVITATION, t);
        }),
        (e.prototype.addEventListener = function (t, n) {
          s[t] = n;
        }),
        e
      );
    })();
  function c(t) {
    r[t] && delete r[t];
  }
  function p(t) {
    return 'string' == typeof t;
  }
  return new ((function () {
    function t() {
      (this._zoomSdk = new u({
        postMessage: this.postMessage,
        version: '0.5.0',
      })),
        this.setEventListener();
      // t.isMacOs(window) || t.isWindows(window)
      //   ? ((this._zoomSdk = new u({
      //       postMessage: this.postMessage,
      //       version: '0.5.0',
      //     })),
      //     this.setEventListener())
      //   : console.warn('JS SDK does not support this platform');
    }
    return (
      (t.prototype.setEventListener = function () {
        if (window.android) {
          window.addEventListener('message', this.zoomSdk.native2js);
        } else {
          t.isWindows(window) &&
            window.chrome.webview.addEventListener('message', this.zoomSdk.native2js);
        }
      }),
      (t.isWindows = function (t) {
        return t.chrome && !!t.chrome.webview;
      }),
      (t.isMacOs = function (t) {
        return t.webkit;
      }),
      (t.prototype.postMessage = function (n) {
        try {
          var e = JSON.stringify(n);
          // console.warn('json::', e);
          if (window.android) {
            window.android.postMessage(e);
          } else {
            t.isMacOs(window)
              ? window.webkit.messageHandlers.jsOCHelper.postMessage(e)
              : t.isWindows(window) && window.chrome.webview.postMessage(e);
          }
        } catch (t) {
          console.log('postmessage faied: ' + t);
        }
      }),
      Object.defineProperty(t.prototype, 'zoomSdk', {
        get: function () {
          return this._zoomSdk;
        },
        enumerable: !1,
        configurable: !0,
      }),
      t
    );
  })())().zoomSdk;
})();

function reloadApp() {
  const apiParam = {
    service: 'chat',
    data: JSON.stringify({
      action: 'DLAct_ReloadSearchApp',
      params: {},
    }),
  };
  zoomSdk.callZoomApi('zmAppBridgePub', apiParam);
}

setTimeout(() => {
  const theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)');
  if (theme && theme.matches) {
    var firstPage = document.getElementsByClassName('first-page');
    if (firstPage && firstPage.length > 0) {
      firstPage[0].className = 'first-page dark';
    }
  }

  var firstLoad = document.getElementById('firstload');
  if (firstLoad) {
    document.getElementsByClassName('first-page')[0].className += ' error';
    document.getElementById('reloadAct').addEventListener('click', reloadApp);
  }
}, 150000);
