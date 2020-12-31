import React, {Fragment} from 'react'
import {message, Progress, Spin, Table} from 'antd'
import NavBar from './NavBar'
import Footer from './Footer'
import Login from './Login'
import './SysInfo.css'
import axios from "axios";

const ramColumns = [
  {
    title: 'Subject',
    dataIndex: 'subject',
    key: 'subject',
    align: 'center'
  },
  {
    title: 'Total',
    dataIndex: 'total',
    key: 'total',
    align: 'center'
  },
  {
    title: 'Used',
    dataIndex: 'used',
    key: 'used',
    align: 'center'
  },
  {
    title: 'Used Percent',
    dataIndex: 'usedPercent',
    key: 'usedPercent',
    align: 'center'
  },
  {
    title: 'Free',
    dataIndex: 'free',
    key: 'free',
    align: 'center'
  },
  {
    title: 'Free Percent',
    dataIndex: 'freePercent',
    key: 'freePercent',
    align: 'center'
  },
];

const cpuColumns = [
  {
    title: 'CPU',
    key: 'cpu',
    align: 'center',
    render: (text, record) => `CPU${record.index}`
  },
  {
    title: 'User',
    dataIndex: 'user',
    key: 'user',
    align: 'center'
  },
  {
    title: 'System',
    dataIndex: 'system',
    key: 'system',
    align: 'center'
  },
  {
    title: 'Wait',
    dataIndex: 'wait',
    key: 'wait',
    align: 'center'
  },
  {
    title: 'Error',
    dataIndex: 'error',
    key: 'error',
    align: 'center'
  },
  {
    title: 'Total Used',
    dataIndex: 'total',
    key: 'total',
    align: 'center'
  },
  {
    title: 'Idle',
    dataIndex: 'idle',
    key: 'idle',
    align: 'center'
  },
]

const diskColumns = [
  {
    title: 'FS Name',
    dataIndex: 'name',
    key: 'name',
    align: 'center'
  },
  {
    title: 'FS Type',
    dataIndex: 'type',
    key: 'type',
    align: 'center'
  },
  {
    title: 'Total',
    dataIndex: 'total',
    key: 'total',
    align: 'center'
  },
  {
    title: 'Used',
    dataIndex: 'used',
    key: 'used',
    align: 'center'
  },
  {
    title: 'Used Percent',
    dataIndex: 'usedPercent',
    key: 'usedPercent',
    align: 'center'
  },
  {
    title: 'Free',
    dataIndex: 'free',
    key: 'free',
    align: 'center'
  },
  {
    title: 'Availiable',
    dataIndex: 'available',
    key: 'available',
    align: 'center'
  },
  {
    title: 'Files',
    dataIndex: 'files',
    key: 'files',
    align: 'center'
  }
]


class SysInfo extends React.Component {
    constructor(props) {
      super(props)
      this.state = {
        ready: false,
        ramData: [],
        memUsage: 0,
        cpuData: [],
        cpuUsage: 0,
        diskData: [],
        diskUsage: 0,
        bandUsage: 0,
        bandwidth: {},
        needLogin: false,
      }
      this.getOption = this.getOption.bind(this)
      this.onLoginSuccess = this.onLoginSuccess.bind(this)
      this.getSysInfoData = this.getSysInfoData.bind(this)
    }

    render() {
        return (
            <Fragment>
                <NavBar current="sys-info"/>
              { !this.state.ready ?
                <Spin size="large" tip="loading..." className="sysinfo-loading"/> :
                <div id = "sysinfo-content">
                {/*绘制仪表盘及表格*/}
                <div id = 'sysinfo-dashboard-area'>
                  <div className = 'sysinfo-dashboard sysinfo-dashboard-first'>
                  <Progress type="dashboard" percent={this.state.cpuUsage} strokeWidth={8} strokeColor={{
                    '0%': '#ff0000',
                    '100%': '#00ff00',
                  }}/>
                  <div>CPU usage</div>
                  </div>

                  <div className = 'sysinfo-dashboard'>
                  <Progress type="dashboard" percent={this.state.memUsage} strokeWidth={8} strokeColor={{
                    '0%': '#ff0000',
                    '100%': '#00ff00',
                  }}/>
                  <div>Mem usage</div>
                  </div>

                  <div className = 'sysinfo-dashboard'>
                  <Progress type="dashboard" percent={this.state.diskUsage} strokeWidth={8} strokeColor={{
                    '0%': '#ff0000',
                    '100%': '#00ff00',
                  }}/>
                  <div>Disk usage</div>
                  </div>

                  <div className = 'sysinfo-dashboard'>
                  <Progress type="dashboard" percent={this.state.bandUsage} strokeWidth={8} strokeColor={{
                    '0%': '#ff0000',
                    '100%': '#00ff00',
                  }}/>
                  <div>Bandwidth usage</div>
                  </div>
                </div>

                <Table bordered title={() => (<span className="table-title">CPU Usage</span>)}
                  dataSource={this.state.cpuData} columns={cpuColumns}
                  rowKey={row => row.index} pagination={false} size = "small" className = "sysinfo-table"/>

                <Table bordered title={() => (<span className="table-title">RAM Usage</span>)}
                  dataSource={this.state.ramData} columns={ramColumns}
                  rowKey={row => row.subject} pagination={false} size = "small" className = "sysinfo-table"/>

                <Table bordered title={() => (<span className="table-title">Disk Usage</span>)}
                  dataSource={this.state.diskData} columns={diskColumns}
                  rowKey={row => row.name} pagination={false} size = "small" className = "sysinfo-table"/>

                {/*<ReactEcharts option={this.state.bandwidth} notMerge={true} lazyUpdate={true} */}
                {/*  className = "sysinfo-table sysinfo-chart"/>*/}

                </div>}
                {this.state.needLogin ?
                    (<Login onLoginSuccess = {this.onLoginSuccess}/>) : (<div></div>)
                }
                <Footer />
            </Fragment>
        )
    }

    getSysInfoData() {
      this.setState({ready: false})
      axios.get("/api/sysInfo").then((res) => {
        if(res.data.code === 302) {
          this.setState({needLogin: true})
        }else if(res.data.code === 200) {
          this.setState({
            ramData: [res.data.data.mem, res.data.data.swap],
            memUsage: res.data.data.mem.usedPercentD,
            cpuData: res.data.data.cpu.detail,
            cpuUsage: res.data.data.cpu.usage,
            diskData: res.data.data.disk.detail,
            diskUsage: res.data.data.disk.usage,
            bandUsage: res.data.data.bandwidth.usedPercent,
            bandwidth: this.getOption(res.data.data.bandwidth.date, res.data.data.bandwidth.incoming, res.data.data.bandwidth.outgoing),
          })
        }
        else {
          message.error(`get sysInfo error: ${res.data.msg}`)
        }
        this.setState({ready: true})
      }).catch((error) => {
          message.error(`get sysInfo error: ${error.message}`)
          this.setState({ready: true})
      })
    }

    onLoginSuccess(){
      this.setState({needLogin: false, dataReady: false})
      this.getSysInfoData()
    }

    componentDidMount(){
      this.getSysInfoData()
    }

    getOption(date, incoming, outgoing) {
      return {
        title: {
          show: true,
          text: "Bandwidth Usage In Latest 30 Days",
          left: "center"
        },
        grid:{
          left:'20px',
          right:'50px',
          bottom:'0',
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
              type: 'cross',
              crossStyle: {
                  color: '#999'
              }
            }
        },
        legend: {
          data:['Incoming','Outgoing'],
          left: "right"
        },
        xAxis: {
          name: 'Date',
          type: 'category',
          data: date,
          axisLabel: {
            interval: 0,
            rotate: 40,
          },
          axisPointer: {
            type: 'shadow'
          }
        },
        yAxis: {
          name: 'Bandwidth(MB)',
          type: 'value'
        },
        series: [
          {
            name: 'Incoming',
            data: incoming,
            type: 'bar',
            barMaxWidth: '10px'
          },
          {
            name: 'Outgoing',
            data: outgoing,
            type: 'bar',
            barMaxWidth: '10px'
          },
        ]}
    }
}

export default SysInfo;