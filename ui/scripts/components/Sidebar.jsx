import React, {Component} from 'react';

import "./Sidebar.css";

class Sidebar extends Component {
    constructor(props) {
        super(props);
    }

    getTerritoryName = () => {
        if (this.props.selectedTerritory === undefined) return;
        else return this.props.selectedTerritory.name;
    };

    getTerritoryOwner = () => {
        if (this.props.selectedTerritory === undefined) return;
        else return this.props.selectedTerritory.owner.name;
    };

    getTerritoryArmies = () => {
        if (this.props.selectedTerritory === undefined) return;
        else return this.props.selectedTerritory.armies;
    };

    render() {
        return (
            <div className="containerDiv">
                <h3>Current Player Turn: {this.props.currPlayer}</h3>
                <h3>Armies Left: {this.props.armiesLeftToAssign}</h3>
                <h3>Territory: {this.getTerritoryName()}</h3>
                <h3>Owner: {this.getTerritoryOwner()}</h3>
                <h3>Armies: {this.getTerritoryArmies()}</h3>
                <button onClick={this.props.handleEndTurn}>End Turn</button>
            </div>
        );
    }
}

export default Sidebar;