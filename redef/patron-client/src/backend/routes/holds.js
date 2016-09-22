const bodyParser = require('body-parser')
const jsonParser = bodyParser.json()

module.exports = (app) => {
  const fetch = require('../fetch')(app)
  app.post('/api/v1/holds', jsonParser, (request, response) => {
    fetch('http://xkoha:8081/api/v1/holds', {
      method: 'POST',
      body: JSON.stringify({
        borrowernumber: Number(request.session.borrowerNumber),
        biblionumber: Number(request.body.recordId),
        branchcode: request.body.branchCode
      })
    }).then(res => {
      if (res.status === 201) {
        response.sendStatus(201)
      } else if (res.status === 403) {
        response.sendStatus(403)
      } else {
        throw Error('Could not reserve publication')
      }
    }).catch(error => {
      console.log(error)
      response.sendStatus(500)
    })
  })

  app.delete('/api/v1/holds', jsonParser, (request, response) => {
    fetch(`http://xkoha:8081/api/v1/holds/${request.body.reserveId}`, {
      method: 'DELETE'
    }).then(res => {
      if (res.status === 200) {
        response.sendStatus(200)
      } else {
        throw Error('Could not delete reservation')
      }
    }).catch(error => {
      console.log(error)
      response.sendStatus(500)
    })
  })
}
