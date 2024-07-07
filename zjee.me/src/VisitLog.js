import React, {Fragment} from 'react'
import ReactEcharts from 'echarts-for-react'
import { DatePicker, Table, Button, message, Spin } from 'antd'
import axios from 'axios'
import NavBar from './NavBar'
import Footer from './Footer'
import moment from 'moment'
import './VisitLog.css'
import Login from './Login'

const visitColumn = [
  {
    title: 'IP',
    dataIndex: 'ip',
    key: 'ip',
    align: 'center'
  },
  {
    title: 'Visit Count',
    dataIndex: 'visitCount',
    key: 'visitCount',
    align: 'center'
  },
  {
    title: 'Location',
    dataIndex: 'location',
    key: 'location',
    align: 'center'
  },
]

class VisitLog extends React.Component {
  constructor(pros){
    super(pros)
    this.state = {
      visitDate: moment().format('YYYY-MM-DD'),
      visitLog: {},
      pvList: {},
      dataReady: false,
      needLogin: false,
    }
    this.handleDateChange = this.handleDateChange.bind(this)
    this.handleQueryClick = this.handleQueryClick.bind(this)
    this.onLoginSuccess = this.onLoginSuccess.bind(this)
  }

  render() {
      return (
          <Fragment>
              <NavBar current="visit-log"/>
              {this.state.dataReady ? (
              <div id="visit-content">
                <ReactEcharts option={this.state.pvList} notMerge={true} lazyUpdate={true} 
                  className = "visit-chart"/>
                <div id = "visit-date">
                  <DatePicker onChange = {this.handleDateChange} defaultValue={moment()}/>
                  <Button type="primary" onClick={this.handleQueryClick} className = "visit-query">Query</Button>
                </div>
                <Table bordered title={() => (<span className="table-title">Visit List</span>)}  
                  dataSource={this.state.visitLog.visitorList} columns={visitColumn} 
                  size = "small" className = "visit-table"/>
              </div>
              ) : 
              (<Spin size="large" tip="loading..."  className="visitlog-loading"/>)}
              {this.state.needLogin ? (
              <Login onLoginSuccess = {this.onLoginSuccess}/>
              ) : (<div></div>)}
              <Footer />
          </Fragment>
      )
  }

  handleQueryClick(e){
    axios.get(`/api/visitLog/${this.state.visitDate}`).then(res => {
      if(res.data.code === 302) {
        this.setState({needLogin: true})
      }else if(res.data.code === 200) {
        this.setState({visitLog: res.data.data,
          pvList: this.getOption(res.data.data.latestPv.dateList, res.data.data.latestPv.pvList)})
      }else{
        message.error(res.data.msg)
      }
      this.setState({dataReady: true})
    }).catch (err => {
      message.error(err.message)
      this.setState({dataReady: true})
    })
  }

  onLoginSuccess(){
    this.setState({needLogin: false, dataReady: false})
    this.handleQueryClick()
  }

  handleDateChange(date, dateString){
    this.setState({visitDate: dateString})
  }

  componentDidMount() {
    this.setState({dataReady: false})
    this.handleQueryClick()
  }

  getOption(x, y) {
    return {
      title: {
        show: true,
        text: "PV In Latest 30 Days",
        left: "center",
      },
      grid:{
        left:'30px',
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
        data: x,
        axisLabel: {
          interval: 0,
          rotate: 40,
        },
        axisPointer: {
          type: 'shadow'
        }
      },
      yAxis: {
        name: 'PV',
        type: 'value'
      },
      series: [{
        name: "PV",
        data: y,
        type: 'bar',
        barMaxWidth: '10px'
      }]}
  }
}

export default VisitLog;