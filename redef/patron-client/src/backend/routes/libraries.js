const bodyParser = require('body-parser')
const jsonParser = bodyParser.json()

module.exports = (app) => {
  const fetch = require('../fetch')(app)
  app.get('/api/v1/libraries', jsonParser, (request, response) => {
    fetch('http://xkoha:8081/api/v1/libraries')
    .then(res => {
      if (res.status === 200) {
        return res.json()
      } else {
        response.status(res.status).send(res.statusText)
        throw Error()
      }
    }).then(json => response.status(200).send(json))
      .catch(error => {
        console.log(error)
        response.sendStatus(500)
      })
  })
}
