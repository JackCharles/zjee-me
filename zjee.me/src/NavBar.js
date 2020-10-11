import React from 'react';
import { Menu, Icon } from 'antd'
import { Link } from 'react-router-dom'
import 'antd/dist/antd.css'
import './NavBar.css'

class NavBar extends React.Component {
    render() {
        return (
            <Menu selectedKeys={[this.props.current]}
                mode="horizontal" id = "nav-bar" theme="dark">
            
                <Menu.Item key="blog" className = "nav-item">
                    <a href = "https://jackcharles.github.io" target = "_blank" rel='noreferrer noopener'>
                        <Icon type="read" />
                        Blog
                    </a>
                </Menu.Item>

                <Menu.Item key="download" className = "nav-item">
                    <Link to="/download">
                        <Icon type="download" />
                        Download
                    </Link>
                </Menu.Item>

                <Menu.Item key="visit-log" className = "nav-item">
                    <Link to="/visit-log">
                        <Icon type="fund" />
                        Visit Log
                    </Link>
                </Menu.Item>

                <Menu.Item key="sys-info" className = "nav-item">
                    <Link to="/sys-info">
                        <Icon type="dashboard" />
                        System Status
                    </Link>
                </Menu.Item>

                <Menu.Item key="home" className = "nav-item">
                    <Link to="/home">
                        <Icon type="home" />
                        Home
                    </Link>
                </Menu.Item>

                <img src="/static/pic/logo.png" alt="ZJEE" height="40px"/>
            </Menu>
        )
    }
}

export default NavBar;
