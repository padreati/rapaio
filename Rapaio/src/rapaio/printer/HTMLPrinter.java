/*
 * Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.printer;

import rapaio.graphics.base.Figure;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class HTMLPrinter extends AbstractPrinter {

    private final String title;
    private final PrintWriter writer;
    private int textWidth = 80;
    private int graphicWidth = 500;
    private int graphicHeight = 250;

    public HTMLPrinter(String fileName, String title) {
        this.title = title;
        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(fileName))));
        } catch (IOException ex) {
            throw new RuntimeException("Could not initialize HTML document", ex);
        }
    }

    @Override
    public int getTextWidth() {
        return textWidth;
    }

    @Override
    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    @Override
    public int getGraphicWidth() {
        return graphicWidth;
    }

    @Override
    public void setGraphicWidth(int graphicWidth) {
        this.graphicWidth = graphicWidth;
    }

    @Override
    public int getGraphicHeight() {
        return graphicHeight;
    }

    @Override
    public void setGraphicHeight(int graphicHeight) {
        this.graphicHeight = graphicHeight;
    }

    @Override
    public void preparePrinter() {
        writer.append(Template.header.replace(Template.KEY_TITLE, title));
    }

    @Override
    public void closePrinter() {
        writer.append(Template.footer);
        writer.flush();
        writer.close();
    }

    @Override
    public void print(String message) {
        writer.append(message + "</br>\n");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void draw(Figure figure, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = newImage.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        figure.paint(g2d, new Rectangle(newImage.getWidth(), newImage.getHeight()));
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(newImage, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not produce image", e);
        }
        writer.append("<p><center><img src=\"data:image/png;base64," + imageString + "\" alt=\"graphics\"/></center></p>\n");
    }

    @Override
    public void error(String message, Throwable throwable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void heading(int h, String lines) {
        writer.append("<h" + h + ">" + lines + "</h" + h + ">\n");
    }

    @Override
    public void code(String lines) {
        writer.append("<pre><code class=\"Java\">" + lines + "</code></pre>\n");
    }

    @Override
    public void p(String lines) {
        writer.append("<p>" + lines + "</p>");
    }

    @Override
    public void eqn(String equation) {
        writer.append("$$ " + equation + " $$");
    }
}

class Template {

    static final String KEY_TITLE = "#TITLE#";
    static final String header = "<!DOCTYPE html>\n"
            + "<html><head>\n"
            + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n"
            + "<title>#TITLE#</title>\n"
            + "<style type=\"text/css\">\n"
            + "body, td {\n"
            + "   font-family: sans-serif;\n"
            + "   background-color: white;\n"
            + "   font-size: 14px;\n"
            + "   margin: 8px;\n"
            + "   width: 800px;\n"
            + "}\n"
            + "tt, code, pre {\n"
            + "   font-family: 'DejaVu Sans Mono', 'Droid Sans Mono', 'Lucida Console', Consolas, Monaco, monospace;\n"
            + "}\n"
            + "h1 { font-size:2.2em; }\n"
            + "h2 { font-size:1.8em; }\n"
            + "h3 { font-size:1.4em; }\n"
            + "h4 { font-size:1.0em; }\n"
            + "h5 { font-size:0.9em; }\n"
            + "h6 { \n"
            + "   font-size:0.8em; \n"
            + "}\n"
            + "a:visited {\n"
            + "   color: rgb(50%, 0%, 50%);\n"
            + "}\n"
            + "pre {	\n"
            + "   margin-top: 0;\n"
            + "   max-width: 95%;\n"
            + "   border: 1px solid #ccc;\n"
            + "   white-space: pre-wrap;\n"
            + "}\n"
            + "pre code {\n"
            + "   display: block; padding: 0.5em;\n"
            + "}\n"
            + "code.r, code.java {\n"
            + "   background-color: #F8F8F8;\n"
            + "}\n"
            + "table, td, th {\n"
            + "  border: none;\n"
            + "}\n"
            + "blockquote {\n"
            + "   color:#666666;\n"
            + "   margin:0;\n"
            + "   padding-left: 1em;\n"
            + "   border-left: 0.5em #EEE solid;\n"
            + "}\n"
            + "hr {\n"
            + "   height: 0px;\n"
            + "   border-bottom: none;\n"
            + "   border-top-width: thin;\n"
            + "   border-top-style: dotted;\n"
            + "   border-top-color: #999999;\n"
            + "}\n"
            + "@media print {\n"
            + "   * { \n"
            + "      background: transparent !important; \n"
            + "      color: black !important; \n"
            + "      filter:none !important; \n"
            + "      -ms-filter: none !important; \n"
            + "   }\n"
            + "   body { \n"
            + "      font-size:12pt; \n"
            + "      max-width:100%; \n"
            + "   }\n"
            + "       \n"
            + "   a, a:visited { \n"
            + "      text-decoration: underline; \n"
            + "   }\n"
            + "   hr { \n"
            + "      visibility: hidden;\n"
            + "      page-break-before: always;\n"
            + "   }\n"
            + "   pre, blockquote { \n"
            + "      padding-right: 1em; \n"
            + "      page-break-inside: avoid; \n"
            + "   }\n"
            + "   tr, img { \n"
            + "      page-break-inside: avoid; \n"
            + "   }\n"
            + "   img { \n"
            + "      max-width: 100% !important; \n"
            + "   }\n"
            + "   @page :left { \n"
            + "      margin: 15mm 20mm 15mm 10mm; \n"
            + "   }\n"
            + "     \n"
            + "   @page :right { \n"
            + "      margin: 15mm 10mm 15mm 20mm; \n"
            + "   }\n"
            + "   p, h2, h3 { \n"
            + "      orphans: 3; widows: 3; \n"
            + "   }\n"
            + "   h2, h3 { \n"
            + "      page-break-after: avoid; \n"
            + "   }\n"
            + "}\n"
            + "</style>\n"
            + "<!-- Styles for R syntax highlighter -->\n"
            + "<style type=\"text/css\">\n"
            + "   pre .operator,\n"
            + "   pre .paren {\n"
            + "     color: rgb(104, 118, 135)\n"
            + "   }\n"
            + "   pre .literal {\n"
            + "     color: rgb(88, 72, 246)\n"
            + "   }\n"
            + "   pre .number {\n"
            + "     color: rgb(0, 0, 205);\n"
            + "   }\n"
            + "   pre .comment {\n"
            + "     color: rgb(76, 136, 107);\n"
            + "   }\n"
            + "   pre .keyword {\n"
            + "     color: rgb(0, 0, 255);\n"
            + "   }\n"
            + "   pre .identifier {\n"
            + "     color: rgb(0, 0, 0);\n"
            + "   }\n"
            + "   pre .string {\n"
            + "     color: rgb(3, 106, 7);\n"
            + "   }\n"
            + "</style>\n"
            + "<!-- R syntax highlighter -->\n"
            + "<script type=\"text/javascript\">\n"
            + "var hljs=new function(){function m(p){return p.replace(/&/gm,\"&amp;\").replace(/</gm,\"&lt;\")}function f(r,q,p){return RegExp(q,\"m\"+(r.cI?\"i\":\"\")+(p?\"g\":\"\"))}function b(r){for(var p=0;p<r.childNodes.length;p++){var q=r.childNodes[p];if(q.nodeName==\"CODE\"){return q}if(!(q.nodeType==3&&q.nodeValue.match(/\\s+/))){break}}}function h(t,s){var p=\"\";for(var r=0;r<t.childNodes.length;r++){if(t.childNodes[r].nodeType==3){var q=t.childNodes[r].nodeValue;if(s){q=q.replace(/\\n/g,\"\")}p+=q}else{if(t.childNodes[r].nodeName==\"BR\"){p+=\"\\n\"}else{p+=h(t.childNodes[r])}}}if(/MSIE [678]/.test(navigator.userAgent)){p=p.replace(/\\r/g,\"\\n\")}return p}function a(s){var r=s.className.split(/\\s+/);r=r.concat(s.parentNode.className.split(/\\s+/));for(var q=0;q<r.length;q++){var p=r[q].replace(/^language-/,\"\");if(e[p]){return p}}}function c(q){var p=[];(function(s,t){for(var r=0;r<s.childNodes.length;r++){if(s.childNodes[r].nodeType==3){t+=s.childNodes[r].nodeValue.length}else{if(s.childNodes[r].nodeName==\"BR\"){t+=1}else{if(s.childNodes[r].nodeType==1){p.push({event:\"start\",offset:t,node:s.childNodes[r]});t=arguments.callee(s.childNodes[r],t);p.push({event:\"stop\",offset:t,node:s.childNodes[r]})}}}}return t})(q,0);return p}function k(y,w,x){var q=0;var z=\"\";var s=[];function u(){if(y.length&&w.length){if(y[0].offset!=w[0].offset){return(y[0].offset<w[0].offset)?y:w}else{return w[0].event==\"start\"?y:w}}else{return y.length?y:w}}function t(D){var A=\"<\"+D.nodeName.toLowerCase();for(var B=0;B<D.attributes.length;B++){var C=D.attributes[B];A+=\" \"+C.nodeName.toLowerCase();if(C.getValue!==undefined&&C.getValue!==false&&C.getValue!==null){A+='=\"'+m(C.getValue)+'\"'}}return A+\">\"}while(y.length||w.length){var v=u().splice(0,1)[0];z+=m(x.substr(q,v.offset-q));q=v.offset;if(v.event==\"start\"){z+=t(v.node);s.push(v.node)}else{if(v.event==\"stop\"){var p,r=s.length;do{r--;p=s[r];z+=(\"</\"+p.nodeName.toLowerCase()+\">\")}while(p!=v.node);s.splice(r,1);while(r<s.length){z+=t(s[r]);r++}}}}return z+m(x.substr(q))}function j(){function q(x,y,v){if(x.compiled){return}var u;var s=[];if(x.k){x.lR=f(y,x.l||hljs.IR,true);for(var w in x.k){if(!x.k.hasOwnProperty(w)){continue}if(x.k[w] instanceof Object){u=x.k[w]}else{u=x.k;w=\"keyword\"}for(var r in u){if(!u.hasOwnProperty(r)){continue}x.k[r]=[w,u[r]];s.push(r)}}}if(!v){if(x.bWK){x.b=\"\\\\b(\"+s.join(\"|\")+\")\\\\s\"}x.bR=f(y,x.b?x.b:\"\\\\B|\\\\b\");if(!x.e&&!x.eW){x.e=\"\\\\B|\\\\b\"}if(x.e){x.eR=f(y,x.e)}}if(x.i){x.iR=f(y,x.i)}if(x.r===undefined){x.r=1}if(!x.c){x.c=[]}x.compiled=true;for(var t=0;t<x.c.length;t++){if(x.c[t]==\"self\"){x.c[t]=x}q(x.c[t],y,false)}if(x.starts){q(x.starts,y,false)}}for(var p in e){if(!e.hasOwnProperty(p)){continue}q(e[p].dM,e[p],true)}}function d(B,C){if(!j.called){j();j.called=true}function q(r,M){for(var L=0;L<M.c.length;L++){if((M.c[L].bR.exec(r)||[null])[0]==r){return M.c[L]}}}function v(L,r){if(D[L].e&&D[L].eR.test(r)){return 1}if(D[L].eW){var M=v(L-1,r);return M?M+1:0}return 0}function w(r,L){return L.i&&L.iR.test(r)}function K(N,O){var M=[];for(var L=0;L<N.c.length;L++){M.push(N.c[L].b)}var r=D.length-1;do{if(D[r].e){M.push(D[r].e)}r--}while(D[r+1].eW);if(N.i){M.push(N.i)}return f(O,M.join(\"|\"),true)}function p(M,L){var N=D[D.length-1];if(!N.t){N.t=K(N,E)}N.t.lastIndex=L;var r=N.t.exec(M);return r?[M.substr(L,r.getIndex-L),r[0],false]:[M.substr(L),\"\",true]}function z(N,r){var L=E.cI?r[0].toLowerCase():r[0];var M=N.k[L];if(M&&M instanceof Array){return M}return false}function F(L,P){L=m(L);if(!P.k){return L}var r=\"\";var O=0;P.lR.lastIndex=0;var M=P.lR.exec(L);while(M){r+=L.substr(O,M.getIndex-O);var N=z(P,M);if(N){x+=N[1];r+='<span class=\"'+N[0]+'\">'+M[0]+\"</span>\"}else{r+=M[0]}O=P.lR.lastIndex;M=P.lR.exec(L)}return r+L.substr(O,L.length-O)}function J(L,M){if(M.sL&&e[M.sL]){var r=d(M.sL,L);x+=r.keyword_count;return r.getValue}else{return F(L,M)}}function I(M,r){var L=M.cN?'<span class=\"'+M.cN+'\">':\"\";if(M.rB){y+=L;M.buffer=\"\"}else{if(M.eB){y+=m(r)+L;M.buffer=\"\"}else{y+=L;M.buffer=r}}D.push(M);A+=M.r}function G(N,M,Q){var R=D[D.length-1];if(Q){y+=J(R.buffer+N,R);return false}var P=q(M,R);if(P){y+=J(R.buffer+N,R);I(P,M);return P.rB}var L=v(D.length-1,M);if(L){var O=R.cN?\"</span>\":\"\";if(R.rE){y+=J(R.buffer+N,R)+O}else{if(R.eE){y+=J(R.buffer+N,R)+O+m(M)}else{y+=J(R.buffer+N+M,R)+O}}while(L>1){O=D[D.length-2].cN?\"</span>\":\"\";y+=O;L--;D.length--}var r=D[D.length-1];D.length--;D[D.length-1].buffer=\"\";if(r.starts){I(r.starts,\"\")}return R.rE}if(w(M,R)){throw\"Illegal\"}}var E=e[B];var D=[E.dM];var A=0;var x=0;var y=\"\";try{var s,u=0;E.dM.buffer=\"\";do{s=p(C,u);var t=G(s[0],s[1],s[2]);u+=s[0].length;if(!t){u+=s[1].length}}while(!s[2]);if(D.length>1){throw\"Illegal\"}return{r:A,keyword_count:x,getValue:y}}catch(H){if(H==\"Illegal\"){return{r:0,keyword_count:0,getValue:m(C)}}else{throw H}}}function g(t){var p={keyword_count:0,r:0,getValue:m(t)};var r=p;for(var q in e){if(!e.hasOwnProperty(q)){continue}var s=d(q,t);s.language=q;if(s.keyword_count+s.r>r.keyword_count+r.r){r=s}if(s.keyword_count+s.r>p.keyword_count+p.r){r=p;p=s}}if(r.language){p.second_best=r}return p}function i(r,q,p){if(q){r=r.replace(/^((<[^>]+>|\\t)+)/gm,function(t,w,v,u){return w.replace(/\\t/g,q)})}if(p){r=r.replace(/\\n/g,\"<br>\")}return r}function n(t,w,r){var x=h(t,r);var v=a(t);var y,s;if(v){y=d(v,x)}else{return}var q=c(t);if(q.length){s=document.createElement(\"pre\");s.innerHTML=y.getValue;y.getValue=k(q,c(s),x)}y.getValue=i(y.getValue,w,r);var u=t.className;if(!u.match(\"(\\\\s|^)(language-)?\"+v+\"(\\\\s|$)\")){u=u?(u+\" \"+v):v}if(/MSIE [678]/.test(navigator.userAgent)&&t.tagName==\"CODE\"&&t.parentNode.tagName==\"PRE\"){s=t.parentNode;var p=document.createElement(\"div\");p.innerHTML=\"<pre><code>\"+y.getValue+\"</code></pre>\";t=p.firstChild.firstChild;p.firstChild.cN=s.cN;s.parentNode.replaceChild(p.firstChild,s)}else{t.innerHTML=y.getValue}t.className=u;t.result={language:v,kw:y.keyword_count,re:y.r};if(y.second_best){t.second_best={language:y.second_best.language,kw:y.second_best.keyword_count,re:y.second_best.r}}}function o(){if(o.called){return}o.called=true;var r=document.getElementsByTagName(\"pre\");for(var p=0;p<r.length;p++){var q=b(r[p]);if(q){n(q,hljs.tabReplace)}}}function l(){if(window.addEventListener){window.addEventListener(\"DOMContentLoaded\",o,false);window.addEventListener(\"load\",o,false)}else{if(window.attachEvent){window.attachEvent(\"onload\",o)}else{window.onload=o}}}var e={};this.LANGUAGES=e;this.highlight=d;this.highlightAuto=g;this.fixMarkup=i;this.highlightBlock=n;this.initHighlighting=o;this.initHighlightingOnLoad=l;this.IR=\"[a-zA-Z][a-zA-Z0-9_]*\";this.UIR=\"[a-zA-Z_][a-zA-Z0-9_]*\";this.NR=\"\\\\b\\\\d+(\\\\.\\\\d+)?\";this.CNR=\"\\\\b(0[xX][a-fA-F0-9]+|(\\\\d+(\\\\.\\\\d*)?|\\\\.\\\\d+)([eE][-+]?\\\\d+)?)\";this.BNR=\"\\\\b(0b[01]+)\";this.RSR=\"!|!=|!==|%|%=|&|&&|&=|\\\\*|\\\\*=|\\\\+|\\\\+=|,|\\\\.|-|-=|/|/=|:|;|<|<<|<<=|<=|=|==|===|>|>=|>>|>>=|>>>|>>>=|\\\\?|\\\\[|\\\\{|\\\\(|\\\\^|\\\\^=|\\\\||\\\\|=|\\\\|\\\\||~\";this.ER=\"(?![\\\\s\\\\S])\";this.BE={b:\"\\\\\\\\.\",r:0};this.ASM={cN:\"string\",b:\"'\",e:\"'\",i:\"\\\\n\",c:[this.BE],r:0};this.QSM={cN:\"string\",b:'\"',e:'\"',i:\"\\\\n\",c:[this.BE],r:0};this.CLCM={cN:\"comment\",b:\"//\",e:\"$\"};this.CBLCLM={cN:\"comment\",b:\"/\\\\*\",e:\"\\\\*/\"};this.HCM={cN:\"comment\",b:\"#\",e:\"$\"};this.NM={cN:\"number\",b:this.NR,r:0};this.CNM={cN:\"number\",b:this.CNR,r:0};this.BNM={cN:\"number\",b:this.BNR,r:0};this.inherit=function(r,s){var p={};for(var q in r){p[q]=r[q]}if(s){for(var q in s){p[q]=s[q]}}return p}}();hljs.LANGUAGES.cpp=function(){var a={keyword:{\"false\":1,\"int\":1,\"float\":1,\"while\":1,\"private\":1,\"char\":1,\"catch\":1,\"export\":1,virtual:1,operator:2,sizeof:2,dynamic_cast:2,typedef:2,const_cast:2,\"const\":1,struct:1,\"for\":1,static_cast:2,union:1,namespace:1,unsigned:1,\"long\":1,\"throw\":1,\"volatile\":2,\"static\":1,\"protected\":1,bool:1,template:1,mutable:1,\"if\":1,\"public\":1,friend:2,\"do\":1,\"return\":1,\"goto\":1,auto:1,\"void\":2,\"enum\":1,\"else\":1,\"break\":1,\"new\":1,extern:1,using:1,\"true\":1,\"class\":1,asm:1,\"case\":1,typeid:1,\"short\":1,reinterpret_cast:2,\"default\":1,\"double\":1,register:1,explicit:1,signed:1,typename:1,\"try\":1,\"this\":1,\"switch\":1,\"continue\":1,wchar_t:1,inline:1,\"delete\":1,alignof:1,char16_t:1,char32_t:1,constexpr:1,decltype:1,noexcept:1,nullptr:1,static_assert:1,thread_local:1,restrict:1,_Bool:1,complex:1},built_in:{std:1,string:1,cin:1,cout:1,cerr:1,clog:1,stringstream:1,istringstream:1,ostringstream:1,auto_ptr:1,deque:1,list:1,queue:1,stack:1,vector:1,map:1,set:1,bitset:1,multiset:1,multimap:1,unordered_set:1,unordered_map:1,unordered_multiset:1,unordered_multimap:1,array:1,shared_ptr:1}};return{dM:{k:a,i:\"</\",c:[hljs.CLCM,hljs.CBLCLM,hljs.QSM,{cN:\"string\",b:\"'\\\\\\\\?.\",e:\"'\",i:\".\"},{cN:\"number\",b:\"\\\\b(\\\\d+(\\\\.\\\\d*)?|\\\\.\\\\d+)(u|U|l|L|ul|UL|f|F)\"},hljs.CNM,{cN:\"preprocessor\",b:\"#\",e:\"$\"},{cN:\"stl_container\",b:\"\\\\b(deque|list|queue|stack|vector|map|set|bitset|multiset|multimap|unordered_map|unordered_set|unordered_multiset|unordered_multimap|array)\\\\s*<\",e:\">\",k:a,r:10,c:[\"self\"]}]}}}();hljs.LANGUAGES.r={dM:{c:[hljs.HCM,{cN:\"number\",b:\"\\\\b0[xX][0-9a-fA-F]+[Li]?\\\\b\",e:hljs.IMMEDIATE_RE,r:0},{cN:\"number\",b:\"\\\\b\\\\d+(?:[eE][+\\\\-]?\\\\d*)?L\\\\b\",e:hljs.IMMEDIATE_RE,r:0},{cN:\"number\",b:\"\\\\b\\\\d+\\\\.(?!\\\\d)(?:i\\\\b)?\",e:hljs.IMMEDIATE_RE,r:1},{cN:\"number\",b:\"\\\\b\\\\d+(?:\\\\.\\\\d*)?(?:[eE][+\\\\-]?\\\\d*)?i?\\\\b\",e:hljs.IMMEDIATE_RE,r:0},{cN:\"number\",b:\"\\\\.\\\\d+(?:[eE][+\\\\-]?\\\\d*)?i?\\\\b\",e:hljs.IMMEDIATE_RE,r:1},{cN:\"keyword\",b:\"(?:tryCatch|library|setGeneric|setGroupGeneric)\\\\b\",e:hljs.IMMEDIATE_RE,r:10},{cN:\"keyword\",b:\"\\\\.\\\\.\\\\.\",e:hljs.IMMEDIATE_RE,r:10},{cN:\"keyword\",b:\"\\\\.\\\\.\\\\d+(?![\\\\w.])\",e:hljs.IMMEDIATE_RE,r:10},{cN:\"keyword\",b:\"\\\\b(?:function)\",e:hljs.IMMEDIATE_RE,r:2},{cN:\"keyword\",b:\"(?:if|in|break|next|repeat|else|for|return|switch|while|try|stop|warning|require|attach|detach|source|setMethod|setClass)\\\\b\",e:hljs.IMMEDIATE_RE,r:1},{cN:\"literal\",b:\"(?:NA|NA_integer_|NA_real_|NA_character_|NA_complex_)\\\\b\",e:hljs.IMMEDIATE_RE,r:10},{cN:\"literal\",b:\"(?:NULL|TRUE|FALSE|T|F|Inf|NaN)\\\\b\",e:hljs.IMMEDIATE_RE,r:1},{cN:\"identifier\",b:\"[a-zA-Z.][a-zA-Z0-9._]*\\\\b\",e:hljs.IMMEDIATE_RE,r:0},{cN:\"operator\",b:\"<\\\\-(?!\\\\s*\\\\d)\",e:hljs.IMMEDIATE_RE,r:2},{cN:\"operator\",b:\"\\\\->|<\\\\-\",e:hljs.IMMEDIATE_RE,r:1},{cN:\"operator\",b:\"%%|~\",e:hljs.IMMEDIATE_RE},{cN:\"operator\",b:\">=|<=|==|!=|\\\\|\\\\||&&|=|\\\\+|\\\\-|\\\\*|/|\\\\^|>|<|!|&|\\\\||\\\\$|:\",e:hljs.IMMEDIATE_RE,r:0},{cN:\"operator\",b:\"%\",e:\"%\",i:\"\\\\n\",r:1},{cN:\"identifier\",b:\"`\",e:\"`\",r:0},{cN:\"string\",b:'\"',e:'\"',c:[hljs.BE],r:0},{cN:\"string\",b:\"'\",e:\"'\",c:[hljs.BE],r:0},{cN:\"paren\",b:\"[[({\\\\])}]\",e:hljs.IMMEDIATE_RE,r:0}]}};\n"
            + "hljs.initHighlightingOnLoad();\n"
            + "</script>\n"
            + "<!-- MathJax scripts -->\n"
            + "<script type=\"text/javascript\" src=\"https://c328740.ssl.cf1.rackcdn.com/mathjax/2.0-latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n";
    static final String tmp = ""
            + "<h1>Explore buldozers</h1>\n"
            + "\n"
            + "<h2>Data sets and other files</h2>\n"
            + "\n"
            + "<p>There are three data sets involved:</p>\n"
            + "\n"
            + "<ol>\n"
            + "<li>train data set, used for learning (Train.csv)</li>\n"
            + "<li>validation data set used for submit and evaluation until final contest week (Valid.csv)</li>\n"
            + "<li>test data set, will be published 1 week befor contest end</li>\n"
            + "</ol>\n"
            + "\n"
            + "<p>Other files involved contains:</p>\n"
            + "\n"
            + "<ol>\n"
            + "<li>machine data appendix contains information about the type of model machine (Machine-Appendix.csv)</li>\n"
            + "</ol>\n"
            + "\n"
            + "<h2>Submission</h2>\n"
            + "\n"
            + "<p>The submission data will contain two fields: SalesID and SalePrice.</p>\n"
            + "\n"
            + "<p>The evaluation function is root mean log squared error. As far as I understood that means:\n"
            + "\\[  RMLSE = \\sum_{i=1}^n{(log(y_i)-log(\\hat{y_i}))^2}  \\]\n"
            + "That cand be easily solved by transforming the SalePrince in log(SalePrice) and getting RMSE as usual.\n"
            + "\\[  RMSE = \\sum_{i=1}^n{(y_i-\\hat{y_i})^2}  \\]</p>\n"
            + "\n"
            + "<h1>Exploratory data analysis</h1>\n"
            + "\n"
            + "<h2>SalePrice</h2>\n"
            + "\n"
            + "<p>Taking into account how the error is evaluated, I will need to transform SalePrice with log.</p>\n"
            + "\n"
            + "<pre><code class=\"r\">par(mfrow = c(1, 2))\n"
            + "hist(t$SalePrice, breaks = 30, col = &quot;pink&quot;)\n"
            + "hist(log(t$SalePrice), breaks = 30, col = &quot;pink&quot;)\n"
            + "</code></pre>\n"
            + "\n"
            + "<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAtAAAAEgCAMAAACq87QqAAAAk1BMVEX9/v0AAAAAADkAAGUAOTkAOWUAOY8AZo8AZrU5AAA5ADk5AGU5OWU5OY85ZrU5j485j7U5j9plAABlADllAGVlOQBlOTllZjlltdpltf2POQCPOTmPOWWPZo+PtY+P27WP29qP2/21ZgC1jzm124+1/rW1/v3ajzna/rXa/tra/v39tWX924/9/rX9/tr9/v3/wMt/me0/AAAAMXRSTlP//////////////////////////////////////////////////////////////wD/obxc3gAAAAlwSFlzAAALEgAACxIB0t1+/AAAEVJJREFUeJztnWuDo7YVhsNMMplJumntTdram213nHS8Gd/2//+6ogs3W5I56AjE4X0+LLNGHODwWAgZxHcXAATx3dQbAAAnEBqIAkIDUUBoIAoIDUQBoYEoIDQQBYQGooDQQBQQGogCQgNRQGggCggNRAGhgSggNBAFhAaigNBAFBAaiAJCA1FAaCAKCA1EAaGBKCA0EAWEBqKQJ/R+NfUWLI6cUj6q0Kf149vlct4+vpm/KnabiKDHl+LhtQ5zWhfF0/ul9Ud71fV/CsXGNW+OJM2rLzt7NZc15ccXhi/GREJ3Pt4VMYmvl1Z/mLw9X5o/Oquu/6Oza78H8ydpXj1Cn9bP7CnfMVQsU9bQZR1QPL6dtyYN+3KivqHl/x//t356P28f/lUm4GDScCh+LD9/2zdfclNeL62qBRPmUDz98XcT++ldr8Qub1a9N5XEaa2W2BUrs47/qnlqeZ3PfasimQkp89rKXDtK+cGGPeWHIr6KnlBo86V9+sskfleY77fO0vcvKvFq7ldT6P1QVNhvsS1/JXSZ1D90UsqDujGrNMvrVZtlVu3smnXYw1+0ysyJlHm9tDLXifLwyp5yUyiOkYWucqd2Vn2j9cfqxHV80d/ph9fjS7mL+0In/rla7PHtUKgFio0qoj6ry3ebHDo5tiapj5E5ynaF5rDb859Zh5mnM7upy4yZl1iS5tWEvI5SCcqaco68Tyi0/p/aA5WWg85yeR47mP01dUC1kEr8s85C9Wld/kponVZ9QHc2ulneHDybVLMdZSATzcwzlXJdZsy8xJI0ryY711HMt4Y55dVGxDB1G7qwdYEv8bpM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\" alt=\"plot of chunk unnamed-chunk-1\"/> </p>\n"
            + "\n"
            + "<p>We can see in previous histograms that price will have an approximate normal distribution after taking the log. It is an expected behavior due to the fact that usual the high prices are fewer than prices below mean. We can see the skewness of the data also in summary of the SalePrice and on log(SalePrice).</p>\n"
            + "\n"
            + "<pre><code class=\"r\">summary(t$SalePrice)\n"
            + "</code></pre>\n"
            + "\n"
            + "<pre><code>##    Min. 1st Qu.  Median    Mean 3rd Qu.    Max. \n"
            + "##    4750   14500   24000   31100   40000  142000\n"
            + "</code></pre>\n"
            + "\n"
            + "<pre><code class=\"r\">summary(log(t$SalePrice))\n"
            + "</code></pre>\n"
            + "\n"
            + "<pre><code>##    Min. 1st Qu.  Median    Mean 3rd Qu.    Max. \n"
            + "##    8.47    9.58   10.10   10.10   10.60   11.90\n"
            + "</code></pre>";
    static final String footer = "</body></html>";
}