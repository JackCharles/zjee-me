import React, {Fragment} from 'react'
import {message, Progress, Spin, Table} from 'antd'
import NavBar from './NavBar'
import Footer from './Footer'
import Login from './Login'
import './SysInfo.css'
import axios from "axios";
import ReactEcharts from "echarts-for-react";

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
    dataIndex: 'name',
    key: 'name',
    align: 'center',
  },
  {
    title: 'USER',
    dataIndex: 'USER',
    key: 'user',
    align: 'center'
  },
  {
    title: 'NICE',
    dataIndex: 'NICE',
    key: 'nice',
    align: 'center'
  },
  {
    title: 'SYSTEM',
    dataIndex: 'SYSTEM',
    key: 'system',
    align: 'center'
  },
  {
    title: 'IDLE',
    dataIndex: 'IDLE',
    key: 'idle',
    align: 'center'
  },
  {
    title: 'IOWAIT',
    dataIndex: 'IOWAIT',
    key: 'io-wait',
    align: 'center'
  },
  {
    title: 'IRQ',
    dataIndex: 'IRQ',
    key: 'irq',
    align: 'center'
  },
  {
    title: 'SOFTIRQ',
    dataIndex: 'SOFTIRQ',
    key: 'softirq',
    align: 'center'
  },
  {
    title: 'STEAL',
    dataIndex: 'STEAL',
    key: 'steal',
    align: 'center'
  }
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
    title: 'Available',
    dataIndex: 'available',
    key: 'available',
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

                <ReactEcharts option={this.state.bandwidth} notMerge={true} lazyUpdate={true}
                  className = "sysinfo-table sysinfo-chart"/>

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
            bandwidth: this.getOption(res.data.data.bandwidth.dt, res.data.data.bandwidth.dataUsage)
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

    getOption(date, dataUsage) {
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
            name: 'Data Usage',
            data: dataUsage,
            type: 'bar',
            barMaxWidth: '10px'
          },
        ]}
    }
}

export default SysInfo;