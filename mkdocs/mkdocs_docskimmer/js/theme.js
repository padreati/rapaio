/*
   docSkimmer theme v0.4.0
   License: BSD-2-Clause license (https://github.com/hfagerlund/mkdocs-docskimmer/blob/master/LICENSE)
*/

const MenuPanel = (function() {
  'use strict';

  const datajs = function (selector) {
      return document.querySelector('[data-js=' + selector + ']');
  };

  const datajsMulti = function (selector) {
      return document.querySelectorAll('[data-js=' + selector + ']');
  };

  const containerPageToc = datajs('toc');
  const containerMainContent = datajs('mainContent');
  const menuOpenControl = datajs('menuOpenCtrl');
  const menuCloseControl = datajs('tocCloseCtrl');
  const pageTocLinks = datajsMulti('tocLink');

  function _bindEventListeners(options){
    ['click', 'keydown'].forEach(function(e) {
      menuCloseControl.addEventListener(e, _menuCloseControlEventHandler);
      menuOpenControl.addEventListener(e, _menuOpenControlEventHandler);
    });

    for (var i = 0; i < pageTocLinks.length; i++) {
      ['click', 'keydown'].forEach(function(e) {
        pageTocLinks[i].addEventListener(e, _menuCloseControlEventHandler);
      });
    }
  }

  function _hideControl(ctrl){
    ctrl.style.visibility = "hidden";
  }

  function _menuOpenControlEventHandler(event) {
    containerPageToc.style.width = "17em";
    containerMainContent.style.marginLeft = "19em";
  }

  function _menuCloseControlEventHandler(event) {
    //only respond to Enter or Space keys, or to click event
    if((event.keyCode === 13 || event.keyCode === 32) || (event.type === 'click')){
      containerPageToc.style.width = "0";
      containerMainContent.style.marginLeft = "2em";
      //check whether page-toc link was activated
      if(event.target.href){
        const link = event.target.href,
            anchor = "#";
        if(link.indexOf(anchor) !== -1){
          //scroll activated anchor link to top of page
          window.location.href = event.target.href;
        }
      }
    }
  }

  const main = function() {
    if(containerPageToc){
      _bindEventListeners();
    } else {
      const menuOpenControl = datajs('menuOpenCtrl');
      _hideControl(menuOpenControl);
    }
  }

  return {
    init: main
  };
})();

MenuPanel.init();
