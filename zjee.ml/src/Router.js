import React from 'react'
import { HashRouter, Switch, Route } from 'react-router-dom'
import Home from './Home'
import SysInfo from './SysInfo'
import VisitLog from './VisitLog'
import Download from './Download'

const BasicRoute = () => (
    <HashRouter>
        <Switch>
            <Route exact path="/" component={Home}/>
            <Route exact path="/home" component={Home}/>
            <Route exact path="/sys-info" component={SysInfo}/>
            <Route exact path="/visit-log" component={VisitLog}/>
            <Route exact path="/download" component={Download}/>
        </Switch>
    </HashRouter>
)

export default BasicRoute;