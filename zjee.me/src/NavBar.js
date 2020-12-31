import React from 'react';
import { Menu } from 'antd'
import {
    ControlOutlined,
    DashboardOutlined,
    DownloadOutlined,
    FundProjectionScreenOutlined, HomeOutlined,
    ReadOutlined
} from '@ant-design/icons';
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
                        <ReadOutlined />
                        Blog
                    </a>
                </Menu.Item>

                <Menu.Item key="task-manager" className = "nav-item">
                    <Link to="/task-manager">
                        <ControlOutlined />
                        Task Manager
                    </Link>
                </Menu.Item>

                <Menu.Item key="download" className = "nav-item">
                    <Link to="/download">
                        <DownloadOutlined />
                        Download
                    </Link>
                </Menu.Item>

                {/*<Menu.Item key="visit-log" className = "nav-item">*/}
                {/*    <Link to="/visit-log">*/}
                {/*        <FundProjectionScreenOutlined />*/}
                {/*        Visit Log*/}
                {/*    </Link>*/}
                {/*</Menu.Item>*/}

                <Menu.Item key="sys-info" className = "nav-item">
                    <Link to="/sys-info">
                        <DashboardOutlined />
                        System Status
                    </Link>
                </Menu.Item>

                <Menu.Item key="home" className = "nav-item">
                    <Link to="/home">
                        <HomeOutlined />
                        Home
                    </Link>
                </Menu.Item>

                <img src="/static/pic/logo.png" alt="ZJEE" height="40px"/>
            </Menu>
        )
    }
}

export default NavBar;
