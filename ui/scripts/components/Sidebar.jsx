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
            rollData: undefined,
            inputValue: undefined
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

    getCurrGamePhase = () => {
        if (this.props.currGameState === undefined) return;

        return this.props.currGameState.state;
    };

    getPlayerIndex = () => this.props.gameInfo.turn % this.props.gameInfo.players.length;

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
            this.state.selectDice2.value + '/' + this.props.attackingRegion.id + '/' +
            this.props.attackedRegion.id + '/' + this.props.gameId).then(res => {
            this.setState({
                rollData: res.data
            });
            this.props.handleUpdateArmies();
        });
    };

    /*
    handleAttack = () => {
        axios.get('/setAttackingDice/' + this.state.selectDice1.value + '/' +
            this.props.attackingRegion.id + '/' + this.props.attackedRegion.id + '/' +
            this.props.gameId).then(res => {

                this.setState({
                rollData: res.data
            });

            this.props.handleUpdateArmies();
        });
    };
     */

    /*

    RENAME ATTACKING SOURCE REGION
    RENAME ATTACKED TARGET REGION

     */
    handleFortify = () => {
        if (this.props.attackingRegion === undefined || this.props.attackedRegion === undefined) return;

        let amtMove = parseInt(this.state.inputValue);

        console.log("AMT: " + amtMove);
        console.log("SRC AMT: " + this.props.attackingRegion.armies);

        if ((amtMove < this.props.attackingRegion.armies) && (amtMove > 0)) {
            axios.get('/moveArmies/' + this.props.attackingRegion.id + '/' +
                this.props.attackedRegion.id + '/' + this.state.inputValue + '/' + this.props.gameId).then(res => {
                this.props.handleUpdateArmies();
            });
        }
    };

    handleDefend = () => {
        axios.get('/setDefendingDice/' + this.state.selectDice2.value +
            this.props.gameId).then(res => {
            this.props.handleUpdateArmies();
        });
    };

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
        if (this.props.currGameState === undefined ||
            this.props.currGameState.state !== 'ASSIGNING') return;

        return (
            <div className="headerDiv">
                <p style={{textAlign: "center"}}>ASSIGN</p>
                <hr/>
                <h3>Armies Left: {this.props.armiesLeftToAssign}</h3>
                <button onClick={this.props.handleBeginAttackPhase}>Start Attack Phase</button>
                <button onClick={this.props.handleBeginFortifyPhase}>Start Fortifying Phase</button>
            </div>
        );
    };

    renderAttackView = () => {
        if (this.props.currGameState === undefined ||
            this.props.currGameState.state !== 'ATTACKING') return;

        return (
            <div className="headerDiv">
                <p style={{textAlign: "center"}}>ATTACK</p>
                <hr/>
                <table>
                    <tbody>
                    <tr>
                        <td>
                            <i>Source</i>
                        </td>
                        <td>
                            <i>Target</i>
                        </td>
                    </tr>
                    <tr>
                        <td><b>{this.getRegionName(this.props.attackingRegion)}</b></td>
                        <td><b>{this.getRegionName(this.props.attackedRegion)}</b></td>
                    </tr>
                    </tbody>
                </table>

                <Dropdown className="dice-select"
                          value={this.state.selectDice1}
                          onChange={this.attackerOnDiceSelect}
                          options={diceOptions.slice(0, this.getAttackerMaxRolls())}/>

                <Dropdown className="dice-select" value={this.state.selectDice2}
                          onChange={this.defenderOnDiceSelect}
                          options={diceOptions.slice(0, this.getDefenderMaxRolls())}/>

                {this.renderRollResults()}
                <button onClick={this.handleAttack}>Commence Attack</button>
                <button onClick={this.props.handleBeginFortifyPhase}>Start Fortifying Phase</button>
            </div>
        );
    };

    renderFortifyView = () => {
        if (this.props.currGameState === undefined ||
            this.props.currGameState.state !== 'FORTIFYING') return;

        return (
            <div className="headerDiv">
                <p style={{textAlign: "center"}}>FORTIFY</p>
                <hr/>
                <table>
                    <tbody>
                    <tr>
                        <td>
                            <i>Source</i>
                        </td>
                        <td>
                            <i>Target</i>
                        </td>
                    </tr>
                    <tr>
                        <td><b>{this.getRegionName(this.props.attackingRegion)}</b></td>
                        <td><b>{this.getRegionName(this.props.attackedRegion)}</b></td>
                    </tr>
                    </tbody>
                </table>
                <input value={this.state.inputValue} onChange={evt => this.updateInputValue(evt)}/>Armies<br/>
                <button onClick={this.handleFortify}>Fortify</button>
            </div>
        );
    };

    updateInputValue = evt => {
        this.setState({
            inputValue: evt.target.value
        });
    };

    /*
    renderDefendView = () => {
        if (this.props.currPhase !== 'DEFENDING') return;

        return (
            <div className="headerDiv urgentBackground">
                <p style={{textAlign: "center"}}>DEFEND</p>
                <hr/>
                <h4>Your territory <ins>{"TEST"}</ins> is getting attacked!</h4>
                <p>Defend by selecting # of dice to roll: </p>
                <Dropdown className="dice-select" value={this.state.selectDice2}
                          onChange={this.defenderOnDiceSelect}
                          options={diceOptions.slice(0, this.getDefenderMaxRolls())}/>
                <button onClick={this.handleDefend}>Defend!</button>
            </div>
        )
    };

    */

    render() {
        return (
            <div className="containerDiv">
                <div className="headerDiv">
                    <p style={{textAlign: "center", margin: "0 auto"}}>GAME INFO</p>
                    <hr/>
                    <table>
                        <tbody>
                        <tr>
                            <td><i>Phase</i></td>
                            <td><i>Turn</i></td>
                        </tr>
                        <tr>
                            <td><b>{this.getCurrGamePhase()}</b></td>
                            <td>{this.props.currPlayer}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <div className="headerDiv" style={{backgroundColor: this.getValidStyle()}}>
                    <p style={{textAlign: "center", margin: "0 auto"}}>SELECTED REGION</p>
                    <hr/>
                    <table>
                        <tbody>
                        <tr>
                            <td><i>Name</i></td>
                            <td><i>Owner</i></td>
                            <td><i>Armies</i></td>
                        </tr>
                        <tr>
                            <td><b>{this.getTerritoryProperty('name')}</b></td>
                            <td>{this.getTerritoryProperty('owner')}</td>
                            <td>{this.getTerritoryProperty('armies')}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                {this.renderAssignView()}
                {this.renderAttackView()}
                {this.renderFortifyView()}
                {
                    /*this.renderDefendView()*/
                }

                <button onClick={this.props.handleEndTurn}>End Turn</button>
            </div>
        );
    }
}

export default Sidebar;
