import React, {Fragment} from 'react'
import { Table, Button, message, Tag, Spin } from 'antd'
import axios from 'axios'
import NavBar from './NavBar'
import Footer from './Footer'
import Login from './Login'
import './Download.css'
import {DownloadOutlined, FolderOpenOutlined} from "@ant-design/icons";

class Download extends React.Component {
    constructor(props) {
      super(props)
      this.state = {
        fileList: [],
        dataReady: false,
        needLogin: false,
      }
      //这个当前路径不放到react的state去更新，因为setState是异步执行的，
      //我们可能无法马上拿到最新结果，而下一个页面的数据请求依赖这个数据，这样有可能导致操作无效
      this.currentBasePath = []
      this.downloadColumn = [
        {
          title: 'Name',
          dataIndex: 'name',
          key: 'name',
          align: 'center'
        },
        {
          title: 'Type',
          dataIndex: 'type',
          key: 'type',
          align: 'center'
        },
        {
          title: 'Size',
          dataIndex: 'size',
          key: 'size',
          align: 'center'
        },
        {
          title: 'Operation',
          key: 'opt',
          align: 'center',
          render: (text, record) => {
            return record.type === "directory" ? (
              <Button className="download-opt-button" 
                type="primary" 
                onClick={e => this.onDirOpen(record)}>
                  <FolderOpenOutlined />
              </Button>
            ) : (
              <Button className="download-opt-button" 
                type="primary" 
                onClick={e => this.onFileDownload(record)}>
                  <DownloadOutlined />
              </Button>
            )
          }
        }
      ]
      this.getFileList = this.getFileList.bind(this)
      this.onDirOpen = this.onDirOpen.bind(this)
      this.onFileDownload = this.onFileDownload.bind(this)
      this.onGoBack = this.onGoBack.bind(this)
      this.onLoginSuccess = this.onLoginSuccess.bind(this)
    }

    render() {
        return (
            <Fragment>
                <NavBar current="download"/>
                <div id="download-content">
                  <div className = "download-path-label">
                    Current Path: &nbsp;&nbsp;
                    <Tag color="geekblue">
                      {this.currentBasePath.reduce((res, item) => res + item + "/", "./")}
                    </Tag>
                    <Button className="download-goback-button"type="primary" onClick = {this.onGoBack} 
                      disabled={this.currentBasePath.length<=0}>Go Back</Button>
                  </div>
                  {this.state.dataReady ? (
                    <Table bordered title={() => (<span className="table-title">File List</span>)} 
                      rowKey={row => row.name} dataSource={this.state.fileList} 
                      columns={this.downloadColumn} size = "small" className = "download-table"/>
                  ) : (
                    <Spin size="large" tip="loading..."/>
                  )}
                </div>
                {this.state.needLogin ? (
                  <Login onLoginSuccess = {this.onLoginSuccess}/>
                ) : (<div></div>)}
                <Footer />
            </Fragment>
        )
    }

    onLoginSuccess(){
      this.setState({needLogin: false, dataReady: false})
      this.getFileList([])
    }

    getFileList(currentPath) {
      this.setState({dataReady: false})
      const relativePath = currentPath.reduce((res, item) => res + item + "/", "")
      axios.get(`/api/download/${relativePath}`).then(res => {
        if(res.data.code === 302) {
          this.setState({needLogin: true})
        } else if(res.data.code === 200) {
          this.setState({fileList: res.data.data})
        }else{
          message.error(`get file list error: ${res.data.msg}`)
        }
        this.setState({dataReady: true})
      }).catch(err => {
        message.error(err.message)
        this.setState({dataReady: true})
      })
    }

    componentDidMount(){
      this.getFileList([])
    }

    onDirOpen(record){
      this.currentBasePath.push(record.name)
      this.getFileList(this.currentBasePath)
    }

    onFileDownload(record){
      const relativePath = this.currentBasePath.reduce((res, item) => res + item + "/", "")
      window.open(`/api/download/${relativePath}${record.name}`, '_blank');
    }

    onGoBack(e) {
      if(this.currentBasePath.length > 0) {
        this.currentBasePath.splice(-1)
        this.getFileList(this.currentBasePath)
      }
    }
}

export default Download;