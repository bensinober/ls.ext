const requestProxy = require('express-request-proxy')
const reactDomServer = require('react-dom/server')
const reactRouter = require('react-router')
const match = reactRouter.match
const RouterContext = reactRouter.RouterContext
const routes = require('../../frontend/routes').Routes
import { Provider } from 'react-redux'
import store from '../../frontend/store'
import { IntlProvider } from 'react-intl'
import React from 'react'

module.exports = (app) => {
  require('./auth')(app)
  require('./checkouts')(app)
  require('./holds')(app)
  require('./profile')(app)
  require('./libraries')(app)
  require('./registration')(app)
  require('./search')(app)
  require('./validation')(app)
  require('./terms')(app)

  app.all('/services/*', requestProxy({
    url: 'http://services:8005/*',
    timeout: 30 * 1000 // 30 seconds
  }))

  app.use((req, res) => {
    match({ routes, location: req.url }, (error, redirectLocation, renderProps) => {
      if (error) {
        res.status(500).send(error.message)
      } else if (redirectLocation) {
        res.redirect(302, redirectLocation.pathname + redirectLocation.search)
      } else if (renderProps) {
        console.log('renderProps', renderProps)
        const componentHTML = reactDomServer.renderToString(
          <Provider store={store}>
            <IntlProvider key="intl" locale="en">
              <RouterContext {...renderProps} />
            </IntlProvider>
          </Provider>)
        const HTML =
          `<!DOCTYPE html>
            <html lang="no">
            <head>
                <meta charset="utf-8" />
                <meta http-equiv="x-ua-compatible" content="ie=edge">
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
                <title>Deichmanske bibliotek - søk</title>
            </head>
            <body>
            <div id="app">${componentHTML}</div>
            <script type="text/javascript" src="/dist/bundle.js"></script></body>
            </html>`
        res.end(HTML)
      } else {
        res.status(404).send('Not found')
      }
    })
  })
}
