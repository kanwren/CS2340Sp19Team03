import React, {Component} from 'react';
import Dropdown from 'react-dropdown';
import axios from 'axios';

import 'react-dropdown/style.css'
import "./Sidebar.css";

const options = [
    1, 2, 3
];

class Sidebar extends Component {
    constructor(props) {
        super(props);
        this.state = {
            selectDice1: undefined,
            selectDice2: undefined,
            rollData: undefined
        }
    }

    _onSelect1 = option => {
        this.setState({
            selectDice1: option
        });
    };

    _onSelect2 = option => {
        this.setState({
            selectDice2: option
        });
    };

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

    getRegionName = region => {
        if (region === undefined) return undefined;
        else return region.name;
    };

    renderAssignView = () => {
        if (this.props.currPhase !== 'ASSIGN') return;

        return (
            <div className="headerDiv">
                <h3>Armies Left: {this.props.armiesLeftToAssign}</h3>
                <button onClick={this.props.handleBeginAttackPhase}>Start Attack Phase</button>
            </div>
        );
    };

    handleAttack = () => {
        console.log(this.state.selectDice1);
        // AXIOS call w/ this.state.selectDice1 and this.state.selectDice2
        axios.get('/simulateDiceRolls/' + this.state.selectDice1.value + '/' + this.state.selectDice2.value + '/' +
            this.props.attackingRegion.id + '/' + this.props.attackedRegion.id + '/' + this.props.gameId).then(res => {
            this.setState({
                rollData: res.data
            });
        });
    };

    renderRollResults = () => {
        if (this.state.rollData === undefined)
            return;

        return (
            <React.Fragment>
                <h3>Attacker: {this.state.rollData.attackerRolls}</h3>
                <h3>Defender: {this.state.rollData.defenderRolls}</h3>
                <h3>Attacker loses {this.state.rollData.attackerLost} armies</h3>
                <h3>Defender loses {this.state.rollData.defenderLost} armies</h3>
            </React.Fragment>
        );
    };

    renderAttackView = () => {
        if (this.props.currPhase !== 'ATTACK') return;

        return (
            <div className="headerDiv">
                <table>
                    <tbody>
                    <tr>
                        <td>
                            <h3>Attacker: {this.getRegionName(this.props.attackingRegion)}</h3>
                            <Dropdown className="dice-select" value={this.state.selectDice1} onChange={this._onSelect1}
                                      options={options}/> Dice
                        </td>
                        <td>
                            <h3>To Attack: {this.getRegionName(this.props.attackedRegion)}</h3>
                            <Dropdown className="dice-select" value={this.state.selectDice2} onChange={this._onSelect2}
                                      options={options}/> Dice
                        </td>
                    </tr>
                    </tbody>
                </table>

                {this.renderRollResults()}
                <button onClick={this.handleAttack}>ATTACK</button>
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

                {this.renderAssignView()}
                {this.renderAttackView()}

                <button onClick={this.props.handleEndTurn}>End Turn</button>
            </div>
        );
    }
}

export default Sidebar;