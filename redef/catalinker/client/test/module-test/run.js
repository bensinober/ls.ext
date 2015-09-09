var cp = require('child_process');
var express = require('express');
var path = require('path');

// Serve application and mock service endpoints
var app = express();
app.use("/", express.static(path.join(__dirname, './../../src')));
app.use("/", express.static(path.join(__dirname, './../../lib')));

var server = app.listen(7777);

app.get("/work", function (req, res) {
  res.sendFile(path.join(__dirname, './../../src/index.html'));
});

app.get("/publication", function (req, res) {
  res.sendFile(path.join(__dirname, './../../src/index.html'));
});

app.get("/config", function (req, res) {
  res.json({
    "kohaOpacUri": "http://koha.deichman.no",
    "kohaIntraUri": "http://koha.deichman.no",
    "ontologyUri": "http://127.0.0.1:7777/ontology",
    "resourceApiUri": "http://127.0.0.1:7777/"
  });
});

app.get("/ontology", function (req, res) {
  res.json(
    {
      "@graph" : [{
        "@id" : "deichman:Publication",
        "@type" : "rdfs:Class",
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Utgivelse"
        }, {
          "@language" : "en",
          "@value" : "Publication"
        }]
      }, {
        "@id" : "deichman:Work",
        "@type" : "rdfs:Class",
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Verk"
        }, {
          "@language" : "en",
          "@value" : "Work"
        }]
      }, {
        "@id" : "deichman:biblio",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Work"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Biblio"
        }, {
          "@language" : "en",
          "@value" : "Biblio"
        }],
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
        }
      }, {
        "@id" : "deichman:creator",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Work"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Skaper"
        }, {
          "@language" : "en",
          "@value" : "Creator"
        }],
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#string"
        }
      }, {
        "@id" : "deichman:format",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Publication"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Format"
        }, {
          "@language" : "en",
          "@value" : "Format"
        }],
        "deichman:valuesFrom": { "@id": "http://127.0.0.1:7777/authorized_values/format"},
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#string"
        }
      }, {
        "@id" : "deichman:language",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Publication"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Språk"
        }, {
          "@language" : "en",
          "@value" : "Language"
        }],
        "deichman:valuesFrom": { "@id": "http://127.0.0.1:7777/authorized_values/language"},
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#string"
        }
      }, {
        "@id" : "deichman:name",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "rdfs:Class"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Navn"
        }, {
          "@language" : "en",
          "@value" : "Name"
        }],
        "rdfs:range" : {
          "@id" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"
        }
      }, {
        "@id" : "deichman:publicationOf",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Publication"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Utgivelse av"
        }, {
          "@language" : "en",
          "@value" : "Publication of"
        }],
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#string"
        }
      }, {
        "@id" : "deichman:recordID",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Publication"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Koha post ID"
        }],
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
        },
        "http://data.deichman.no/ui#editable": false
      }, {
        "@id" : "deichman:year",
        "@type" : "rdfs:Property",
        "rdfs:domain" : {
          "@id" : "deichman:Work"
        },
        "rdfs:label" : [{
          "@language" : "no",
          "@value" : "Årstall"
        }, {
          "@language" : "en",
          "@value" : "Year"
        }],
        "rdfs:range" : {
          "@id" : "http://www.w3.org/2001/XMLSchema#gYear"
        }
      }],
      "@context" : {
        "deichman" : "http://127.0.0.1:7777/ontology#",
        "rdfs" : "http://www.w3.org/2000/01/rdf-schema#"
      }
    }
  );
});

app.get("/authorized_values/language", function (req, res) {
  res.send(
    {
      "@graph": [
        {
          "@id": ":eng",
          "@type": ":Language",
          "label": {
            "@language": "no",
            "@value": "Engelsk"
          }
        },
        {
          "@id": ":fra",
          "@type": ":Language",
          "label": {
            "@language": "no",
            "@value": "Fransk"
          }
        },
        {
          "@id": ":nno",
          "@type": ":Language",
          "label": {
            "@language": "no",
            "@value": "Norsk (Nynorsk)"
          }
        },
        {
          "@id": ":nob",
          "@type": ":Language",
          "label": {
            "@language": "no",
            "@value": "Norsk (Bokmål)"
          }
        }
      ],
      "@context": {
        "label": "http://ww.w3.org/2000/01/rdf-schema#label",
        "": "http://lexvo.org/id/iso639-3/",
        "rdfs": "http://ww.w3.org/2000/01/rdf-schema#"
      }
    });
});

app.get("/authorized_values/format", function (req, res) {
  res.send(
    {
      "@graph": [
        {
          "@id": "http://data.deichman.no/format#CD",
          "@type": "http://data.deichman.no/utility#Format",
          "label": {
            "@language": "no",
            "@value": "CD"
          }
        },
        {
          "@id": "http://data.deichman.no/format#Book",
          "@type": "http://data.deichman.no/utility#Format",
          "label": {
            "@language": "no",
            "@value": "Bok"
          }
        },
        {
          "@id": "http://data.deichman.no/format#DVD",
          "@type": "http://data.deichman.no/utility#Format",
          "label": {
            "@language": "no",
            "@value": "DVD"
          }
        }
      ],
      "@context": {
        "label": "http://ww.w3.org/2000/01/rdf-schema#label",
        "rdfs": "http://ww.w3.org/2000/01/rdf-schema#"
      }
    });
});

app.post("/work", function (req, res) {
  res.setHeader("location", "http://127.0.0.1:7777/work/1");
  res.send();
});

app.patch("/work/1", function (req, res) {
  res.send(); // PATCH always 200 OK
});

app.get("/work/1", function (req, res) {
  res.send(JSON.stringify(
    {
      "@id" : "http://127.0.0.1:7777/work/1",
      "@type" : "deichman:Work",
      "@context" : {
        "deichman" : "http://127.0.0.1:7777/ontology#"
      }
    }));
});

app.post("/publication", function (req, res) {
  res.setHeader("location", "http://127.0.0.1:7777/publication/1");
  res.send();
});

app.get("/publication/1", function (req, res) {
  res.send(JSON.stringify(
    {
      "@id" : "http://127.0.0.1:7777/publication/1",
      "@type" : "deichman:Publication",
      "deichman:recordID" : "123",
      "@context" : {
        "deichman" : "http://127.0.0.1:7777/ontology#"
      }
    }));
});

app.patch("/publication/1", function (req, res) {
  res.send(); // PATCH always 200 OK
});


// Launches the CasperJS test suite in a subprocess
var ps = cp.spawn('casperjs', ['--log-level=debug', 'test', 'test/module-test/tests.js']);
ps.stdout.on('data', function (data) {
  console.log(data.toString().replace("\n", ""));
});
ps.stderr.on('data', function (data) {
  console.log(data.toString().replace("\n", ""));
});
ps.on('error', function (err) {
  console.log("failed to launch casperjs: " + err);
});
ps.on('exit', function (code) {
  if (process.argv.indexOf('--keepserver') === -1) {
    server.close();
    process.exit(code);
  }
});
