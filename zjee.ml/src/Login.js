import React from 'react'
import { Form, Icon, Input, Button, Modal } from 'antd';
import cookie from 'react-cookies'
import './Login.css'
import Axios from 'axios';

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
      const { getFieldDecorator } = this.props.form;
      return (
        <Form onSubmit={this.handleSubmit} className="login-form">
          <Form.Item className="login-form-item">
            {getFieldDecorator('username', {
              rules: [{ required: true, message: 'Please input your username!' }],
            })(
              <Input
                prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />}
                placeholder="Username"
              />,
            )}
          </Form.Item>
          <Form.Item className="login-form-item">
            {getFieldDecorator('password', {
              rules: [{ required: true, message: 'Please input your Password!' }],
            })(
              <Input
                prefix={<Icon type="lock" style={{ color: 'rgba(0,0,0,.25)' }} />}
                type="password"
                placeholder="Password"
              />,
            )}
          </Form.Item>
          <Form.Item className="login-form-submit">
            <Button type="primary" htmlType="submit" className="login-form-button" disabled = {!this.state.clickble}>
              {this.state.clickble ? "Log in" : (<span>please wait...<Icon type="loading" /></span>)}
            </Button>
          </Form.Item>
          <div className="login-auth-status">{this.state.authStatus}</div>
        </Form>
      );
    }

    handleSubmit(e) {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
          if (!err) {
            this.setState({clickble: false})
            const formData = new FormData();
            formData.append("username", values.username)
            formData.append("password", values.password)
            Axios.post("/login", formData, {
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).then(res => {
                if(res.data.auth_status === 1) {
                    this.setState({authStatus: res.data.msg})
                }else{
                    this.props.onLoginSuccess()
                }
                this.setState({clickble: true})
            }).catch(err => {
                this.setState({authStatus: err.message})
                this.setState({clickble: true})
            })
          }
        });
      };
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
        const LoginForm = Form.create({ name: 'normal_login' })(NormalLoginForm)
        return (
            <Modal
                title="Login"
                visible={this.state.visible}
                className="login-modal"
                footer={null}
                destroyOnClose={true}
                onCancel={e=>this.setState({visible: false})}
            >
            <LoginForm onLoginSuccess = {this.onLoginSuccess}/>
            </Modal>
        )
    }

    onLoginSuccess() {
        this.setState({visible: false})
        this.props.onLoginSuccess()
    }
}

export default Login