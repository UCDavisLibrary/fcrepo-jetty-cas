var jwt = require('jsonwebtoken');

var token = jwt.sign({ username: 'jrmerz' }, 'testtestest', {'issuer': 'library.ucdavis.edu'});
console.log(token);