import React, {Component} from "react";

import './Topbar.css';

class Topbar extends Component {
    constructor(props) {
        super(props);
        this.state = {}
    }

    getPlayerIndex = () => this.props.gameState.turn % this.props.gameState.players.length;

    getLabelStyling = index => {
        if (index === this.getPlayerIndex())
            return "label selected";
        else return "label";
    };

    renderNames() {
        if (this.props.gameState === undefined || this.props.gameState.players === undefined)
            return;

        return (
            <table>
                <tbody>
                <tr>
                    {this.props.gameState.players.map((el, index) => {
                        return (
                            <td key={index}>
                                <div className={this.getLabelStyling(index)}
                                     style={{backgroundColor: this.props.terrColors[index]}}
                                >
                                    {el.name}
                                </div>
                            </td>
                        );
                    })}
                </tr>
                </tbody>
            </table>
        );
    }

    render() {
        return (
            <div>
                {this.renderNames()}
            </div>
        );
    }
}

export default Topbar;