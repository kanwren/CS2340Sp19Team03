import React, {Component} from 'react';
import scriptLoader from 'react-async-script-loader';
import axios from 'axios';
import Topbar from "./Topbar.jsx"
import Sidebar from "./Sidebar.jsx";

import Tracer from "./Tracer.jsx";

import "./MapComponent.css";

// Territory highlight and border settings
const HIGHLIGHT_OPACITY = 0.5;
const UNHIGHLIGHT_OPACITY = 1.0;
const BORDER_COLOR = "#FFFFFF";
const BORDER_WIDTH = 1.0;
const ARMY_FONT_SIZE = 20;
const allTerrsText = {};

// Initial game dimensions and scale
const ORIG_HEIGHT = 628, ORIG_WIDTH = 1227;
const MAP_TO_WIDTH_SCALE = 0.75;

// Other logistical constants
const playerMap = {};
const TERR_COLORS = ['#51d0ff', '#ff5151', '#51ffa2', '#ffff51', '#af66ff', '#ffa726', '#ff66cc'];

class MapComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            DOMLoaded: false,
            mapInitialized: false,
            mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH,
            terrDatas: undefined,
            armiesLeftToAssign: undefined,
            currGameInfo: undefined,
            currGameState: undefined,
            currPlayer: undefined,
            selectedTerritory: undefined,
            attackingRegion: undefined,
            attackedRegion: undefined,
            adjTerrs: undefined,
            phaseIndex: 0,
            fixedPlayer: undefined,
            tracer: new Tracer(),
            eventFuncs: {}
        }
    }

    /* This must be here */
    componentDidUpdate() {
        if (!this.state.DOMLoaded) {
            this.setState({
                DOMLoaded: true,
                fixedPlayer: this.getPlayerNameFromPath()
            });
        }

        if (!this.state.mapInitialized) {
            this.initializeMap();
        }
    }

    resize = () => this.resizeMap();

    componentDidMount() {
        window.addEventListener('resize', this.resize);

        this.intervalId = setInterval(() => {
            console.log("INTERVAL HERE");
        }, 1000);
    }

    componentWillUnmount() {
        window.removeEventListener('resize', this.resize);
    }

    initializeMap() {
        this.setState({
            mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH,
            mapInitialized: true,
        }, () => {
            this.updateArmyCounts(() => {
                this.setupTerritoriesText();
                this.updateCurrGameState();
                this.updateGameInfo(() => {
                    let players = this.state.currGameInfo.players;
                    players.forEach((e, i) => {
                        playerMap[e.name] = i;
                    });
                    this.setupTerritoriesMouseAction();
                    this.updateTerritoryColors();
                });
            });

            let scale = this.state.mapScaleFactor;
            window.rsr.setViewBox(0, 0, ORIG_WIDTH, ORIG_HEIGHT, true);
            window.rsr.setSize(ORIG_WIDTH * scale, ORIG_HEIGHT * scale);
        });
    }

    resizeMap() {
        this.setState({mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH}, () => {
            let scale = this.state.mapScaleFactor;
            window.rsr.setViewBox(0, 0, ORIG_WIDTH, ORIG_HEIGHT, true);
            window.rsr.setSize(ORIG_WIDTH * scale, ORIG_HEIGHT * scale);
        });
    }

    setMouseOver = (region, isLinked) => {
        let id = this.getRegionId(region);

        this.setState({
            selectedTerritory: this.state.terrDatas[id]
        });

        if (isLinked) {
            for (let i = 0; i < region.length; i++) {
                region[i].node.style.opacity = HIGHLIGHT_OPACITY;
            }
        } else {
            region.node.style.opacity = HIGHLIGHT_OPACITY;
        }
    };

    setMouseOut = (region, isLinked) => {
        let id = this.getRegionId(region);

        this.setState({
            selectedTerritory: undefined
        });

        if (isLinked) {
            for (let i = 0; i < region.length; i++) {
                region[i].node.style.opacity = UNHIGHLIGHT_OPACITY;
            }
        } else {
            region.node.style.opacity = UNHIGHLIGHT_OPACITY;
        }
    };

    getAdjacentTerritoryIds = (terrID, callback) => {
        axios.get('/territoryAdjacencies/' + terrID + '/' + this.getGameIdFromPath()).then(res => {
            this.setState({
                adjTerrs: res.data
            }, callback);
        });
    };

    territoryCanAttack = id => {
        return (this.getCurrentPhase() === 'ATTACKING') && this.state.armiesLeftToAssign === 0
            && (this.state.currPlayer === this.state.terrDatas[id].owner.name);
    };

    territoryCanFortify = id => {
        return (this.getCurrentPhase() === 'FORTIFYING') && this.state.armiesLeftToAssign === 0
            && (this.state.currPlayer === this.state.terrDatas[id].owner.name) &&
            this.state.attackingRegion === undefined;
    };

    territoryCanBeAttacked = id => {
        if (!(this.getCurrentPhase() === 'ATTACKING') || this.state.armiesLeftToAssign > 0 ||
            this.state.attackingRegion === undefined) return false;

        return (this.state.currPlayer !== this.state.terrDatas[id].owner.name) &&
            (this.state.adjTerrs.indexOf(id) !== -1);
    };

    territoryCanBeFortified = id => {
        if (!(this.getCurrentPhase() === 'FORTIFYING') || this.state.armiesLeftToAssign > 0 ||
            this.state.attackingRegion === undefined) return false;

        return (this.state.currPlayer === this.state.terrDatas[id].owner.name) &&
            (this.state.adjTerrs.indexOf(id) !== -1);
    };

    setMouseDown = (region, isLinked) => {
        let id = this.getRegionId(region);

        if (this.territoryCanAttack(id) || this.territoryCanFortify(id)) {
            this.setState({
                attackingRegion: this.state.terrDatas[id],
                attackedRegion: undefined
            });
        }

        if (this.state.attackingRegion !== undefined) {
            this.getAdjacentTerritoryIds(this.state.attackingRegion.id, () => {
                if (this.territoryCanBeAttacked(id) || this.territoryCanBeFortified(id))
                    this.setState({
                        attackedRegion: this.state.terrDatas[id]
                    });
            });
        }

        if (!(this.getCurrentPhase() === 'ATTACKING') && this.state.armiesLeftToAssign > 0 &&
            (this.state.currPlayer === this.state.terrDatas[id].owner.name)) {
            const newTerrDatas = this.state.terrDatas.slice();
            newTerrDatas[id].armies += 1;

            this.setState({
                terrDatas: newTerrDatas,
                armiesLeftToAssign: this.state.armiesLeftToAssign - 1
            }, () => {
                this.setTerritoryText(id, this.state.terrDatas[id].armies);

                this.incrementTerritoryArmyCount(id, 1, () => {
                });
                axios.get('/useArmy/' + this.getGameIdFromPath()).then();
            });
        }
    };

    setupTerritoriesMouseAction() {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');

                if (this.state.terrDatas === undefined) return;
                let owner = this.state.terrDatas[regionId].owner.name;
                for (let j = 0; j < region.length; j++) {
                    region[j].node.style.strokeWidth = BORDER_WIDTH;
                    region[j].node.style.stroke = BORDER_COLOR;
                    region[j].node.setAttribute("regionId", regionId);
                    this.setRegionColor(region[j], TERR_COLORS[playerMap[owner]]);
                }
                region.mouseover(() => this.setMouseOver(region, true)
                ).mouseout(() => this.setMouseOut(region, true)
                ).mousedown(() => this.setMouseDown(region, true));
            } else {
                for (let j = 0; j < region.length; j++) {
                    let regionId = region[j].data('id');

                    if (this.state.terrDatas === undefined) return;
                    let owner = this.state.terrDatas[regionId].owner.name;
                    let terr = region[j];
                    terr.node.style.strokeWidth = BORDER_WIDTH;
                    terr.node.style.stroke = BORDER_COLOR;
                    terr.node.setAttribute("regionId", regionId);
                    this.setRegionColor(terr, TERR_COLORS[playerMap[owner]]);

                    terr.mouseover(() => this.setMouseOver(terr, false)
                    ).mouseout(() => this.setMouseOut(terr, false)
                    ).mousedown(() => this.setMouseDown(terr, false));
                }
            }
        }
    }

    /*
    addMouseOverEventToTerritories = (f) => {

        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.mouseover(f);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.mouseover(f);
                }
            }
        }
    };

    addMouseDownEventToTerritories = (f) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.mousedown(f);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.mousedown(f);
                }
            }
        }
    };

    addMouseUpEventToTerritories = (f) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.mouseup(f);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.mouseup(f);
                }
            }
        }
    };

    removeMouseOverEventToTerritories = (name) => {
        name = this.state.eventFuncs[name];
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.unmouseover(name);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.unmouseover(name);
                }
            }
        }
    };

    removeMouseDownEventToTerritories = (name) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.unmousedown(name);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.unmousedown(name);
                }
            }
        }
    };

    removeMouseUpEventToTerritories = (name) => {
        name = this.state.eventFuncs[name];
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');
                region.unmouseup(name);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    let regionId = terr.data('id');
                    terr.unmouseup(name);
                }
            }
        }
    };
     */

    setRegionColor = (region, color) => {
        region.attr('fill', color);
    };

    updateTerritoryColors() {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');

                if (this.state.terrDatas === undefined) return;
                let owner = this.state.terrDatas[regionId].owner.name;
                for (let j = 0; j < region.length; j++) {
                    this.setRegionColor(region[j], TERR_COLORS[playerMap[owner]]);
                }
            } else {
                for (let j = 0; j < region.length; j++) {
                    let regionId = region[j].data('id');
                    if (this.state.terrDatas === undefined) return;
                    let owner = this.state.terrDatas[regionId].owner.name;
                    this.setRegionColor(region[j], TERR_COLORS[playerMap[owner]]);
                }
            }
        }
    }

    getRegionId = region => {
        if (window.linkedRegions.indexOf(region) !== -1) {
            return region[0].data('id');
        } else {
            return region.data('id');
        }
    };

    setupTerritoriesText = () => {
        for (let i in window.allTerrs) {
            let region = window.allTerrs[i], bbox = region.getBBox();
            let x = (bbox.x + bbox.width / 2), y = (bbox.y + bbox.height / 2);

            let terrID = this.getRegionId(region);
            this.updateArmyCounts(() => {
                allTerrsText[terrID] = window.rsr.text(x, y, this.state.terrDatas[terrID].armies).attr({"font-size": ARMY_FONT_SIZE});
            });
        }
    };

    setTerritoryText = (regionID, stringContent) => {
        allTerrsText[regionID].attr({text: stringContent});
    };

    updateTerritoryById = (terrID, callback) => {
        axios.get('/territoryInfo/' + terrID + '/' + this.getGameIdFromPath()).then(res => {
            const newTerrDatas = this.state.terrDatas.slice();
            newTerrDatas[terrID] = res.data;

            this.setState({
                terrDatas: newTerrDatas
            }, callback);
        });
    };

    updateArmyCounts = callback => {
        axios.get('/territoriesInfo/' + this.getGameIdFromPath()).then(res => {
            this.setState({
                terrDatas: res.data.sort((a, b) => a.id - b.id)
            }, callback);
        });
    };

    updateCurrGameState = () => {
        axios.get('/gameState/' + this.getGameIdFromPath()).then(res => {
            this.setState({
                currGameState: res.data
            }, () => {
                console.log(this.state.currGameState.state);
            });
        });
    };

    updateGameInfo = callback => {
        axios.get('/gameInfo/' + this.getGameIdFromPath()).then(res => {
            let gameInfo = res.data;

            this.setState({
                currGameInfo: gameInfo,
                currPlayer: gameInfo.players[gameInfo.turn % gameInfo.players.length].name,
            });

            if (this.getCurrentPhase() === 'ASSIGNING') {
                axios.get('/gameState/' + this.getGameIdFromPath()).then(res => {
                    this.setState({
                        currGameState: res.data,
                        armiesLeftToAssign: res.data.armiesLeft
                    }, callback);
                });
            } else {
                this.setState({
                    armiesLeftToAssign: 0
                }, callback);
            }
        });
    };

    getGameIdFromPath = () => {
        return window.location.pathname.substring(1);
    };

    getPlayerNameFromPath = () => {
        var location = window.location + ""; // String conversion

        return location.split("playerName=")[1];
    };

    getCurrentPhase = () => {
        if (this.state.currGameState === undefined) return undefined;

        return this.state.currGameState.state;
    };

    /*
    f = e => {
        // console.log("Click!");
        let regionId = e.target.getAttribute('regionId');
        let territory = this.state.terrDatas[regionId];
        if (this.state.currPlayer === territory.owner.name) {
            this.setState({attackingRegion: territory});
            this.getAdjacentTerritoryIds(regionId);
        }
    };

    d = e => {
        let regionId = e.target.getAttribute('regionId');
        let territory = this.state.terrDatas[regionId];
        let attacker = this.state.attackingRegion;

        if ((attacker !== undefined) && (this.state.currPlayer !== territory.owner.name) &&
            (this.state.adjTerrs.indexOf(parseInt(regionId)) !== -1)) {
            this.setState({attackedRegion: territory});
            this.removeMouseDownEventToTerritories(e => this.f(e));
            this.removeMouseUpEventToTerritories(e => this.d(e));
        } else {
            this.setState({attackedRegion: undefined});
            this.setState({attackingRegion: undefined})
        }

        // console.log("Attacker: " + this.state.attackingRegion.name);
        // console.log("Defender: " + this.state.attackedRegion.name);
    };
     */

    beginAttackPhase = () => {
        if (this.state.armiesLeftToAssign === 0) {
            axios.get("/startAttackingPhase/" + this.getGameIdFromPath()).then(() => {
                this.updateCurrGameState();
            });

            /*
            this.state.tracer.turnOnTracerLine();
            this.addMouseDownEventToTerritories(this.f);
            this.addMouseUpEventToTerritories(this.d);
             */
        }
    };

    beginFortifyPhase = () => {
        if (this.state.armiesLeftToAssign === 0) {
            axios.get("/startFortifyingPhase/" + this.getGameIdFromPath()).then(() => {
                this.updateCurrGameState();
                this.setState({
                    attackingRegion: undefined,
                    attackedRegion: undefined
                })
            });

            /*
            this.state.tracer.turnOnTracerLine();
            this.addMouseDownEventToTerritories(this.f);
            this.addMouseUpEventToTerritories(this.d);
             */
        }
    };

    /*
    REQUESTS TO CHANGE BACKEND DATA
    */
    handleEndTurn = () => {
        if (this.state.currGameState.state !== 'ASSIGNING' || this.state.armiesLeftToAssign === 0) {
            this.setState({
                attackedRegion: undefined,
                attackingRegion: undefined,
            });

            axios.get('/endTurn/' + this.getGameIdFromPath()).then(() => {
                this.updateCurrGameState();
                this.updateGameInfo(() => {
                    console.log("Current Player: " + this.state.currPlayer);
                });
            });

            console.log("Turn ended!");
        } else {
            console.log("Cannot end turn!");
        }
    };

    incrementTerritoryArmyCount = (terrID, count, callback) => {
        axios.get('/addArmiesToTerritory/' + count + '/' + terrID + '/' + this.getGameIdFromPath())
            .then(() => callback());
    };

    updateAttackerDefenderTextAndColors = () => {
        if (this.state.attackingRegion === undefined ||
            this.state.attackedRegion === undefined) return;
        let aId = this.state.attackingRegion.id, dId = this.state.attackedRegion.id;

        this.setTerritoryText(aId, this.state.terrDatas[aId].armies);
        this.setTerritoryText(dId, this.state.terrDatas[dId].armies);
        this.updateTerritoryColors();
    };

    render() {
        return (
            <React.Fragment>
                <Topbar
                    gameState={this.state.currGameInfo}
                    terrColors={TERR_COLORS}
                    fixedPlayer={this.state.fixedPlayer}
                />
                <div className="flex-box">
                    <div id="rsr"/>
                    <Sidebar
                        armiesLeftToAssign={this.state.armiesLeftToAssign}
                        currPlayer={this.state.currPlayer}
                        selectedTerritory={this.state.selectedTerritory}
                        handleEndTurn={this.handleEndTurn}
                        handleBeginAttackPhase={this.beginAttackPhase}
                        handleBeginFortifyPhase={this.beginFortifyPhase}
                        isAttackPhase={this.state.isAttackPhase}
                        attackingRegion={this.state.attackingRegion}
                        attackedRegion={this.state.attackedRegion}
                        currGameState={this.state.currGameState}
                        gameId={this.getGameIdFromPath()}
                        handleUpdateArmies={() => this.updateArmyCounts(this.updateAttackerDefenderTextAndColors)}
                        colors={TERR_COLORS}
                        gameInfo={this.state.currGameInfo}
                    />
                </div>
            </React.Fragment>
        );
    }
}

export default scriptLoader(
    'assets/javascripts/raphael-min.js',
    'assets/javascripts/worldMap.js'
)(MapComponent)
