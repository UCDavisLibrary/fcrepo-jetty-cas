var jwt = require('jsonwebtoken');

var token = jwt.sign({ foo: 'bar' }, 'testtestest', {'issuer': 'library.ucdavis.edu'});
console.log(token);