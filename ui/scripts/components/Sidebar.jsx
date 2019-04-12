import React, {Component} from 'react';
import Dropdown from 'react-dropdown';

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
            selectDice2: undefined
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
        if (region === undefined) return;
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
        // AXIOS call w/ this.state.selectDice1 and this.state.selectDice2
        // axios.get('/simulateDiceRolls/' + this.state.selectDice1 + '/' + this.state.selectDice2 + '/' +
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
                            <Dropdown className="dice-select" value={this.state.selectDice1} onChange={this._onSelect1} options={options}/> Dice
                        </td>
                        <td>
                            <h3>To Attack: {this.getRegionName(this.props.attackedRegion)}</h3>
                            <Dropdown className="dice-select" value={this.state.selectDice2} onChange={this._onSelect2} options={options}/> Dice
                        </td>
                    </tr>
                    </tbody>
                </table>
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