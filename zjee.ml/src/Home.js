import React, { Fragment } from 'react';
import axios from 'axios'
import { message } from 'antd'
import 'antd/dist/antd.css'
import './Home.css'
import NavBar from './NavBar'
import Footer from './Footer';

class Home extends React.Component {

  constructor(props) {
    super(props);
    this.state = {}
    this.updateTime = this.updateTime.bind(this)
    this.getBackgroundImage = this.getBackgroundImage.bind(this)
    this.getMotto = this.getMotto.bind(this)
  }

  render() {
    return (
      <Fragment>
        <NavBar current='home' />

        <div id="home-content" style={this.state.bgStyle}>
          <div id="home-content-center">
            <div id="home-date-time">
              <span id="home-date">
                {this.state.year}-
                {this.state.month}-
                {this.state.day}
              </span> &nbsp;&nbsp;&nbsp;&nbsp;
              <span id="home-time">
                {this.state.hours}:
                {this.state.minutes}:
                {this.state.seconds}
              </span>
            </div>
          </div>
          <div id="home-yiyan">{this.state.motto}</div>
        </div>

        <Footer />
      </Fragment>
    )
  }

  updateTime() {
    function PrefixInteger(num, length) {
      return (Array(length).join('0') + num).slice(-length);
    }
    var date = new Date();
    if(this.mounted){
      this.setState({
        year: date.getFullYear(),
        month: PrefixInteger(date.getMonth() + 1, 2),
        day: PrefixInteger(date.getDate(), 2),
        hours: PrefixInteger(date.getHours(), 2),
        minutes: PrefixInteger(date.getMinutes(), 2),
        seconds: PrefixInteger(date.getSeconds(), 2)
      });
    }
  }

  getBackgroundImage(){
    axios.get("/api/webImage?batch=0").then((res) => {
      if(res.data.code === 200) {
        this.setState({bgStyle:{background: `url("${res.data.data}") no-repeat`, backgroundSize: "cover"}})
      }else {
        message.error(`get image error: ${res.data.msg}`);
      }
    }).catch((error) => {
        message.error(`get image error: ${error.message}`);
    })
  }

  getMotto() {
    axios.get("/api/motto").then((res) => {
      if(res.data.code === 200) {
        this.setState({motto: `${res.data.data.hitokoto}    —From ${res.data.data.from}`})
      }else {
        message.error(`get motto error: ${res.data.msg}`);
      }
    }).catch((error) => {
        message.error(`get motto error: ${error.message}`);
    })
  }

  componentDidMount() {
    this.mounted = true
    this.updateTime()
    setInterval(this.updateTime, 1000)
    this.getBackgroundImage()
    this.getMotto()
  }

  componentWillUnmount() {
    this.mounted = false
  }
}

export default Home;
