import React from 'react'
import {Button, Form, Input, Modal} from 'antd';
import './Login.css'
import Axios from 'axios';
import {LoadingOutlined, LockOutlined, UserOutlined} from "@ant-design/icons";

class NormalLoginForm extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            authStatus: "",
            clickble: true
        }
        this.handleSubmit = this.handleSubmit.bind(this)
    }

    render() {
        return (
            <Form
                onFinish={this.handleSubmit}
                className="login-form"
            >
                <Form.Item
                    className="login-form-item"
                    rules={[{required: true, message: 'Please input your username!'}]}
                    name="username"
                >
                    <Input
                        prefix={<UserOutlined style={{color: 'rgba(0,0,0,.25)'}}/>}
                        placeholder="Username"
                    />
                </Form.Item>
                <Form.Item
                    className="login-form-item"
                    rules={[{required: true, message: 'Please input your Password!'}]}
                    name={"password"}
                >
                    <Input
                        prefix={<LockOutlined style={{color: 'rgba(0,0,0,.25)'}}/>}
                        type="password"
                        placeholder="Password"
                    />
                </Form.Item>
                <Form.Item className="login-form-submit">
                    <Button type="primary" htmlType="submit" className="login-form-button"
                            disabled={!this.state.clickble}>
                        {this.state.clickble ? "Log in" : (<span>please wait...<LoadingOutlined/></span>)}
                    </Button>
                </Form.Item>
                <div className="login-auth-status">{this.state.authStatus}</div>
            </Form>
        );
    }

    handleSubmit(values) {
        this.setState({clickble: false})
        const formData = new FormData();
        formData.append("username", values.username)
        formData.append("password", values.password)
        Axios.post("/login", formData, {
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).then(res => {
            if (res.data.auth_status === 1) {
                this.setState({authStatus: res.data.msg})
            } else {
                this.props.onLoginSuccess()
            }
            this.setState({clickble: true})
        }).catch(err => {
            this.setState({authStatus: err.message})
            this.setState({clickble: true})
        })
    }
}

class Login extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            visible: true
        }
        this.onLoginSuccess = this.onLoginSuccess.bind(this)
    }

    render() {
        return (
            <Modal
                title="Login"
                visible={this.state.visible}
                className="login-modal"
                footer={null}
                destroyOnClose={true}
                onCancel={e => this.setState({visible: false})}
            >
                <NormalLoginForm onLoginSuccess={this.onLoginSuccess}/>
            </Modal>
        )
    }

    onLoginSuccess() {
        this.setState({visible: false})
        this.props.onLoginSuccess()
    }
}

export default Login