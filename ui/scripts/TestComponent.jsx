import React, {Component} from 'react'

class TestComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {};
        console.log("This");
    }

    render() {
        return (
            <React.Fragment>
                <h1>Content</h1>
            </React.Fragment>
        );
    }
}

export default TestComponent;