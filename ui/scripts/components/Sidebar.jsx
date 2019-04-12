import React, {Component} from 'react';

import "./Sidebar.css";

class Sidebar extends Component {
    constructor(props) {
        super(props);
    }

    getTerritoryName = () => {
        if (this.props.selectedTerritory === undefined) return;
        return this.props.selectedTerritory.name;
    };

    getTerritoryOwner = () => {
        if (this.props.selectedTerritory === undefined) return;
        return this.props.selectedTerritory.owner.name;
    };

    getTerritoryArmies = () => {
        if (this.props.selectedTerritory === undefined) return;
        return this.props.selectedTerritory.armies;
    };

    getValidStyle = () => {
        if (this.props.selectedTerritory === undefined) return;
        if (this.props.currPlayer === this.props.selectedTerritory.owner.name) {
            return "validTerritoryStyle";
        }
    };

    renderAssignView = () => {
        if (this.props.currPhase !== 'ASSIGN') return;

        return (
            <div className="headerDiv">
                <h3>Armies Left: {this.props.armiesLeftToAssign}</h3>
            </div>
        );
    };

    renderAttackView = () => {
        if (this.props.currPhase !== 'ATTACK') return;

        return (
            <div className="headerDiv">
                <h3>Attacker: {this.props.attackingRegion}</h3>
                <h3>To Attack: {this.props.attackedRegion}</h3>
                <button>ATTACK</button>
            </div>
        );
    };

    render() {
        return (
            <div className="containerDiv">
                <div className="headerDiv">
                    <p style={{textAlign: "center"}}>GAME INFO</p>
                    <h3>PHASE: {this.props.currPhase}</h3>
                    <h3>TURN: {this.props.currPlayer}</h3>
                </div>

                <div className={"headerDiv " + this.getValidStyle()}>
                    <p className="textCenter">SELECTED REGION</p>
                    <h3 className="textCenter">{this.getTerritoryName()}</h3>
                    <hr/>
                    <h3>Owner: {this.getTerritoryOwner()}</h3>
                    <h3>Armies: {this.getTerritoryArmies()}</h3>
                </div>

                <button onClick={this.props.handleBeginAttackPhase}>Start Attack Phase</button>

                {this.renderAssignView()}
                {this.renderAttackView()}

                <button onClick={this.props.handleEndTurn}>End Turn</button>
            </div>
        );
    }
}

export default Sidebar;