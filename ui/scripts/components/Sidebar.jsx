import React, {Component} from 'react';
import Dropdown from 'react-dropdown';
import axios from 'axios';

import 'react-dropdown/style.css'
import './Sidebar.css';

const diceOptions = [
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

    attackerOnDiceSelect = option => {
        this.setState({
            selectDice1: option
        });
    };

    defenderOnDiceSelect = option => {
        this.setState({
            selectDice2: option
        });
    };

    getTerritoryProperty(property) {
        if (this.props.selectedTerritory === undefined) return;

        if (property === 'owner')
            return this.props.selectedTerritory.owner.name;
        else
            return this.props.selectedTerritory[property];
    }

    getPlayerIndex = () => this.props.gameState.turn % this.props.gameState.players.length;

    getValidStyle = () => {
        if (this.props.selectedTerritory === undefined) return;
        if (this.props.currPlayer === this.props.selectedTerritory.owner.name)
            return this.props.colors[this.getPlayerIndex()];
    };

    getRegionName = region => {
        if (region === undefined) return undefined;
        else return region.name;
    };

    handleAttack = () => {
        axios.get('/simulateDiceRolls/' + this.state.selectDice1.value + '/' +
            this.props.attackingRegion.id + '/' + this.props.attackedRegion.id + '/' +
            this.props.gameId).then(res => {
            this.setState({
                rollData: res.data
            });
            this.props.handleUpdateArmies();
        });
    };

    /*
    handleAttack = () => {
        axios.get('/simulateDiceRolls/' + this.state.selectDice1.value + '/' +
            this.state.selectDice2.value + '/' + this.props.attackingRegion.id + '/' +
            this.props.attackedRegion.id + '/' + this.props.gameId).then(res => {
            this.setState({
                rollData: res.data
            });
            this.props.handleUpdateArmies();
        });
    };
    */

    // handleReset = () =>  {
    //     this.setState({
    //         this.props.attackingRegion: undefined,
    //         this.props.attackedRegion: undefined,
    //         selectDice1: undefined,
    //         selectDice2: undefined
    //     }
    // };

    getAttackerMaxRolls = () => {
        if (this.props.attackingRegion === undefined) return;
        let armyCount = this.props.attackingRegion.armies;

        if (armyCount - 1 > 3) return 3;
        else return armyCount - 1;
    };

    getDefenderMaxRolls = () => {
        if (this.props.attackedRegion === undefined) return;
        let armyCount = this.props.attackedRegion.armies;

        if (armyCount > 2) return 2;
        else return armyCount;
    };

    renderRollResults = () => {
        if (this.state.rollData === undefined) return;

        return (
            <React.Fragment>
                <h3>Attacker: {this.state.rollData.attackerRolls.join('-')}</h3>
                <h3>Defender: {this.state.rollData.defenderRolls.join('-')}</h3>
                <h3>Attacker loses {this.state.rollData.attackerLost} armies</h3>
                <h3>Defender loses {this.state.rollData.defenderLost} armies</h3>
            </React.Fragment>
        );
    };

    renderAssignView = () => {
        if (this.props.currPhase !== 'ASSIGN') return;

        return (
            <div className="headerDiv">
                <p style={{textAlign: "center"}}>ASSIGN</p>
                <hr/>
                <h3>Armies Left: {this.props.armiesLeftToAssign}</h3>
                <button onClick={this.props.handleBeginAttackPhase}>Start Attack Phase</button>
            </div>
        );
    };

    renderAttackView = () => {
        if (this.props.currPhase !== 'ATTACK') return;

        return (
            <div className="headerDiv">
                <p style={{textAlign: "center"}}>ATTACK</p>
                <hr/>
                <table>
                    <tbody>
                    <tr>
                        <td>
                            <h3>Attacker: {this.getRegionName(this.props.attackingRegion)}</h3>
                            <h3>To Attack: {this.getRegionName(this.props.attackedRegion)}</h3>
                        </td>
                        <td>
                            <Dropdown className="dice-select"
                                      value={this.state.selectDice1}
                                      onChange={this.attackerOnDiceSelect}
                                      options={diceOptions.slice(0, this.getAttackerMaxRolls())}/> Dice
                        </td>
                    </tr>
                    </tbody>
                </table>

                {this.renderRollResults()}
                <button onClick={this.handleAttack}>Commence Attack</button>
            </div>
        );
    };

    renderFortifyView = () => {
        return (
            <div className="headerDiv">
                <p style={{textAlign: "center"}}>FORTIFY</p>
                <hr/>
                <table>
                    <tbody>
                    <tr>
                        <td>
                            SOURCE
                        </td>
                        <td>
                            DEST
                        </td>
                    </tr>
                    </tbody>
                </table>
                <button>Fortify</button>
            </div>
        );
    };

    renderDefendView = () => {
        return (
            <div className="headerDiv urgentBackground">
                <p style={{textAlign: "center"}}>DEFEND</p>
                <hr/>
                <h4>Your territory <ins>{"TEST"}</ins> is getting attacked!</h4>
                <p>Defend by selecting # of dice to roll: </p>
                <Dropdown className="dice-select" value={this.state.selectDice2}
                          onChange={this.defenderOnDiceSelect}
                          options={diceOptions.slice(0, this.getDefenderMaxRolls())}/>
                <button>Defend!</button>
            </div>
        )
    };

    render() {
        return (
            <div className="containerDiv">
                <div className="headerDiv">
                    <p style={{textAlign: "center", margin: "0 auto"}}>GAME INFO</p>
                    <hr/>
                    <h3>PHASE: {this.props.currPhase}</h3>
                    <h3>TURN: {this.props.currPlayer}</h3>
                </div>

                <div className="headerDiv" style={{backgroundColor: this.getValidStyle()}}>
                    <p style={{textAlign: "center", margin: "0 auto"}}>SELECTED REGION</p>
                    <h3 style={{textAlign: "center"}}>{this.getTerritoryProperty('name')}</h3>
                    <hr/>
                    <h3>Owner: {this.getTerritoryProperty('owner')}</h3>
                    <h3>Armies: {this.getTerritoryProperty('armies')}</h3>
                </div>

                {this.renderAssignView()}
                {this.renderAttackView()}
                {this.renderFortifyView()}
                {this.renderDefendView()}

                <button onClick={this.props.handleEndTurn}>End Turn</button>
            </div>
        );
    }
}

export default Sidebar;
