import React, {Component} from "react";

let lineSvg = '<svg id="lineSvg" height="0" width="0" style="display: none; position: fixed; top: 0px; left: 0px;">' +
    '<line x1="0" y1="0" x2="500" y2="500" style="stroke: rgb(0, 0, 0); stroke-width: 2;"/></svg>';

// let rsrDiv = document.getElementById('react-view');
document.body.innerHTML = document.body.innerHTML + lineSvg;

lineSvg = document.getElementById('lineSvg');
let line = document.querySelector('#lineSvg > line');

let mouseDownX;
let mouseDownY;

class Tracer extends Component {
    turnOnTracerLine = () => {
        document.body.addEventListener('mousedown', this.tracerMouseDown);
        document.body.addEventListener('mouseup', this.tracerMouseUp);
    };

    turnoffTracerLine = () => {
        document.body.removeEventListener('mousedown', tracerMouseDown);
        document.body.removeEventListener('mouseup', tracerMouseUp);
    };

    tracerMouseDown = e => {
        mouseDownX = e.clientX;
        mouseDownY = e.clientY;
        lineSvg.style.top = e.clientY + "px";
        lineSvg.style.left = e.clientX + "px";
        lineSvg.style.display = "initial";
        document.body.addEventListener('mousemove', this.tracerMouseMove);
    };

    tracerMouseUp = e => {
        lineSvg.setAttribute('width', 0);
        lineSvg.setAttribute('height', 0);
        lineSvg.style.display = "none";
        document.body.removeEventListener('mousemove', this.tracerMouseMove);
    };

    tracerMouseMove = e => {
        e.preventDefault();
        let thisWidth = this.tracerWidth(mouseDownX, e.clientX);
        let thisHeight = this.tracerHeight(mouseDownY, e.clientY);
        lineSvg.setAttribute('width', thisWidth);
        lineSvg.setAttribute('height', thisHeight);

        if (mouseDownX <= e.clientX) {
            lineSvg.style.left = mouseDownX + "px";
            line.setAttribute('x1', 0);
            line.setAttribute('x2', thisWidth);
        } else {
            lineSvg.style.left = e.clientX + "px";
            line.setAttribute('x1', thisWidth);
            line.setAttribute('x2', 0);
        }

        if (mouseDownY <= e.clientY) {
            lineSvg.style.top = mouseDownY + "px";
            line.setAttribute('y1', 0);
            line.setAttribute('y2', thisHeight);
        } else {
            lineSvg.style.top = e.clientY + "px";
            line.setAttribute('y1', thisHeight);
            line.setAttribute('y2', 0);
        }
    };

    tracerHeight = (y1, y2) => {
        return Math.abs(y1 - y2);
    };

    tracerWidth = (x1, x2) => {
        return Math.abs(x1 - x2);
    };
}

export default Tracer;
