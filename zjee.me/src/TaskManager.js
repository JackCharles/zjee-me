import React, {Fragment} from 'react'
import {Button, Divider, Form, Input, message, Table, Tooltip} from "antd";
import NavBar from "./NavBar";
import './TaskManager.css'
import axios from "axios";
import {CheckCircleTwoTone, CloseCircleTwoTone} from "@ant-design/icons";
import Footer from "./Footer";
import Login from "./Login";


const layout = {
    labelCol: {
        span: 5,
    },
    wrapperCol: {
        span: 16,
    },
};

const tailLayout = {
    wrapperCol: {
        offset: 8,
        span: 16,
    },
};

const viewLatestLog = () => {
    let logBox = document.getElementById("log-area");
    logBox.scrollTop = logBox.scrollHeight;
}

class TaskManager extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            taskId: "",
            taskLog: "",
            logPageNo: 0,
            logPageSize: 100,
            timerId: -1,
            taskRunning: false,
            taskList: [],
            taskPageNo: 0,
            taskPageSize: 10,
            hasNextPageTask: false,
            needLogin: false
        }

        this.onFinish = this.onFinish.bind(this);
        this.getLog = this.getLog.bind(this);
        this.getTaskList = this.getTaskList.bind(this);
        this.changePage = this.changePage.bind(this);
        this.killTask = this.killTask.bind(this);
        this.onLoginSuccess = this.onLoginSuccess.bind(this);
    }

    componentDidMount() {
        this.getTaskList();
    }

    render() {
        const colRender = c => (<Tooltip title={c}>{c}</Tooltip>);
        return (
            <Fragment>
                <NavBar current="task-manager"/>
                <div className={"content-panel"}>
                    <div className={"left-panel"}>
                        <Divider orientation="left">Submit Task</Divider>
                        <Form
                            {...layout}
                            onFinish={this.onFinish}
                            className={"form-area"}
                        >
                            <Form.Item
                                label="Command"
                                name="cmd"
                                rules={[{required: true, message: 'Please input cmd!'}]}
                            >
                                <Input/>
                            </Form.Item>
                            <Form.Item
                                label="TimeOut"
                                name="timeout"
                            >
                                <Input/>
                            </Form.Item>
                            <Form.Item
                                label="Description"
                                name="desc"
                            >
                                <Input/>
                            </Form.Item>

                            <Form.Item {...tailLayout} className={"task-control-area"}>
                                <Button type="primary"
                                        htmlType="submit"
                                        className={"task-control-btn"}
                                        disabled={this.state.taskRunning || this.state.needLogin}>
                                    Submit Task
                                </Button>

                                <Button type="danger"
                                        className={"task-control-btn"}
                                        disabled={!this.state.taskRunning}
                                        onClick={this.killTask}>
                                    Stop Task
                                </Button>
                            </Form.Item>
                        </Form>

                        <div className={"task-log"}>
                            <Divider orientation="left">Task Log</Divider>
                            <code>
                            <Input.TextArea
                                id={"log-area"}
                                value={this.state.taskLog}
                            />
                            </code>
                        </div>
                    </div>


                    <div className={"right-panel"}>
                        <Divider orientation="left">History</Divider>

                        <Table dataSource={this.state.taskList}
                               bordered
                               rowKey="taskId"
                               pagination={false}
                        >
                            <Table.Column title="Status" dataIndex="exitStatus" key="status" render={
                                s => s === 0 ?
                                    (<CheckCircleTwoTone twoToneColor="#52c41a"/>) :
                                    (<CloseCircleTwoTone twoToneColor="#ff0000"/>)
                            }/>

                            <Table.Column ellipsis={{showTitle: false}} title="Task ID" dataIndex="taskId" key="taskId"
                                          render={colRender}/>
                            <Table.Column ellipsis={{showTitle: false}} title="Command" dataIndex="cmd" key="cmd"
                                          render={colRender}/>
                            <Table.Column ellipsis={{showTitle: false}} title="Start Time" dataIndex="startTime"
                                          key="startTime" render={colRender}/>
                            <Table.Column ellipsis={{showTitle: false}} title="End Time" dataIndex="endTime"
                                          key="endTime" render={colRender}/>
                            <Table.Column ellipsis={{showTitle: false}} title="Description" dataIndex="desc" key="desc"
                                          render={colRender}/>
                        </Table>
                        <div className={"task-list-control"}>
                            <Button type="primary"
                                    disabled={!this.state.taskPageNo > 0}
                                    onClick={() => this.changePage(-1)}>
                                Last Page
                            </Button>

                            <span className={"page-number"}>&sect;&middot; {this.state.taskPageNo + 1} &middot;&sect;</span>

                            <Button type="primary"
                                    disabled={!this.state.hasNextPageTask}
                                    onClick={() => this.changePage(1)}>
                                Next Page
                            </Button>
                        </div>
                    </div>
                </div>

                {this.state.needLogin ?
                    (<Login onLoginSuccess = {this.onLoginSuccess}/>) : (<div></div>)
                }

                <Footer />
            </Fragment>
        );
    }

    onFinish(value) {
        axios.post("/api/task/submit", value).then(res => {
            message.info("Submit Success");
            let data = res.data;
            let log = `Task submit successfully.\nTask id: ${data.taskId}\nTask cmd: ${data.cmd}\nStart time: ${data.startTime}\n`

            // 开启定时器读取日志
            let timerId = setInterval(this.getLog, 2000);
            // 更新状态
            this.setState({taskLog: log, taskId: data.taskId, timerId: timerId, taskRunning: true});
        }).catch(err => {
            message.error(err.message);
            this.setState({
                taskLog: this.state.taskLog +
                    err.message + '\n' + err.response.data
            });
        });
    }
    ;

    getLog() {
        axios.post("/api/task/log", {
            taskId: this.state.taskId,
            pageNo: this.state.logPageNo,
            pageSize: this.state.logPageSize
        }).then(res => {
            let log = res.data;
            let taskRunning = true;
            if (!log) {
                clearInterval(this.state.timerId);
                log += 'Process End!'
                taskRunning = false;
            }
            this.setState({
                taskLog: this.state.taskLog + log,
                logPageNo: this.state.logPageNo + 1,
                taskRunning
            }, viewLatestLog);
        }).catch(err => {
            message.error(err.message);
            clearInterval(this.state.timerId);
            this.setState({
                taskLog: this.state.taskLog +
                    err.message + '\n' + err.response.data,
                taskRunning: false
            }, viewLatestLog);
        });
    }

    getTaskList() {
        let pageSize = this.state.taskPageSize + 1;
        axios.post("/api/task/list", {
            pageNo: this.state.taskPageNo,
            pageSize
        }).then(res => {
            if(res.data.code === 302) {
                this.setState({needLogin: true});
                return;
            }
            this.setState({
                taskList: res.data,
                hasNextPageTask: !!res.data && res.data.length === pageSize,
            })
        }).catch(err => message.error(err.response.data))
    }

    changePage(diff) {
        this.setState({taskPageNo: this.state.taskPageNo + diff},
            () => this.getTaskList());
    }

    killTask() {
        axios.post("/api/task/stop", {taskId: this.state.taskId})
            .then(res => message.info(res.data))
            .catch(err => message.error(err.response.data));
    }

    onLoginSuccess(){
        this.setState({needLogin: false}, ()=>this.getTaskList())
    }
}

export default TaskManager;