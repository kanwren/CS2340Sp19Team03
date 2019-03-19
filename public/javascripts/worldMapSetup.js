// Regions that contain multiple territories that would normally be highlightable
// on their own. We make sure to filter them out in the following for loops
// var linkedRegions = [indonesia, japan, newguinea, greatbritain, india, easternaustralia, argentina];

// Constants
var HIGHLIGHT_OPACITY = 0.5;
var BORDER_COLOR = "#FFFFFF";
var BORDER_WIDTH = 1.0;

function setLinkedRegionMouseHandler(linkedRegion) {
    linkedRegion.mouseover(function (e) {
        for (var j = 0; j < linkedRegion.length; j++) linkedRegion[j].node.style.opacity = HIGHLIGHT_OPACITY;
    }).mouseout(function (e) {
        for (var j = 0; j < linkedRegion.length; j++) linkedRegion[j].node.style.opacity = 1;
    });
}

for (var i in rsrGroups) {
    var region = rsrGroups[i];

    if (linkedRegions.indexOf(region) !== -1) {
        for (var j = 0; j < region.length; j++) {
            region[j].node.style.strokeWidth = BORDER_WIDTH;
            region[j].node.style.stroke = BORDER_COLOR;
        }
        setLinkedRegionMouseHandler(region);
    } else {
        for (var j = 0; j < region.length; j++) {
            var territory = region[j];
            territory.node.style.strokeWidth = BORDER_WIDTH;
            territory.node.style.stroke = BORDER_COLOR;
            territory.mouseover(function (e) {
                this.node.style.opacity = HIGHLIGHT_OPACITY;
            }).mouseout(function (e) {
                this.node.style.opacity = 1;
            });
        }
    }
}

function setTerritoryText() {
    for (var i in allTerrs) {
        var region = allTerrs[i], bbox = region.getBBox();
        var text = undefined;
        var x = bbox.x + bbox.width / 2;
        var y = bbox.y + bbox.height / 2;

        if (linkedRegions.indexOf(region) !== -1) text = rsr.text(x, y, region[0].data('id'));
        else text = rsr.text(x, y, region.data('id'));
    }
}

setTerritoryText();