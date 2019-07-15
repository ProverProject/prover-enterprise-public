# Prover Enterprise Backend API

All API requests use POST method. Parameters must be encoded as
x-www-form-urlencoded as described in RFC 1867. All responses are JSON
objects (as described in RFC 8259) containing either "result" or
"error" field.

Only the following HTTP error codes are used:

- "`400 Bad Request`" is used when HTTP request has wrong format or some
  request parameters are missing or have invalid type (i.e. contain letters
  where numbers are expected).
- "`404 Path Not Found`" is used to indicate invalid request name.
- "`405 Method Not Allowed`" is used when method name is other than POST.
- "`411 Length Required`" is used if Content-Length request header is missing.
- "`413 Request Entity Too Large`" is used when request body size is more than
  10000 bytes (it's more than enough for all implemented requests).
- "`415 Unsupported Media Type`" is used when content type is not
  `application/x-www-form-urlencoded`.
- "`500 Internal Server Error`" is used when something unexpected happens
  during the request execution.

All other errors are returned as JSON objects within "`200 OK`" HTTP responses.

## /ent/v1/get-status

This RPC method is used to query the current status of the Software.

### Parameters

None.

### Response

    {
      "result":
      {
        "version": "dd62ec7",
        "index":
        {
          "networkTopBlock": null,
          "indexTopBlock": 1920419,
          "targetTopBlock": null
        },
        "keystore":
        {
          "state": "locked",
          "address": "NBEZUMIEHSSLSABFQ64S3YWTV5LCQ5RV5LUB7S62"
        },
        "alert-message": null
      }
    }

## /ent/v1/get-balance

This RPC method is used to query the balance of the wallet controlled by The
Software.

### Parameters

None.

### Response

    {
      "result":
      {
        "address": "NADOELOAL2V5U6BIYPWCQ34POIP4HEQNSHBKOTBN",
        "balance":
        [
          {
            "mosaicId":
            {
              "namespaceId": "nem",
              "name": "xem"
            },
            "quantity": 95100000
          },
          {
            "mosaicId":
            {
              "namespaceId": "prover",
              "name": "proof"
            },
            "quantity": 17550000000
          }
        ]
      }
    }

## /ent/v1/estimate-fee

This call is used to calculate the exact price of some operation, including
required token amount and transaction fee.

### Parameters

- request - one of "`request-swype-code`", "`submit-message`",
  "`submit-media-hash`";
- other parameters - the same as of corresponding requests.

### Response

    {
      "result":
      [
        {
          "mosaicId":
          {
            "namespaceId": "nem",
            "name": "xem"
          },
          "quantity": 150000
        },
        {
          "mosaicId":
          {
            "namespaceId": "prover",
            "name": "proof"
          },
          "quantity": 100000000
        }
      ]
    }

## /ent/v1/request-swype-code

This RPC method should be called at least twice. The first time it's called
to issue a transaction. Transaction hash is returned to the client. The
second and subsequent calls are used to query status of the transaction. When
the transaction becomes confirmed, swype code is generated and returned.

### Parameters (action)

None.

### Parameters (polling for swype-code)

- txhash - hash returned by the first call.

### Response (action)

    {
      "result":
      {
        "txhash": "90cf2524dfa43f9197225afb765862a36e436b0194608ec8987591c8220eb9d7"
      }
    }

### Response (polling, transaction is not confirmed)

    {
      "result":
      {
        "swype-id": null,
        "swype-sequence": null,
        "swype-seed": null
      }
    }

### Response (polling, transaction is confirmed)

    {
      "result":
      {
        "swype-id": 5960639,
        "swype-sequence": "*47134376",
        "swype-seed": "98b181ebf3827746a4a22ff18e060c415202b93c434c5dce5113e9e3a3c73c7f"
      }
    }

## /ent/v1/fast-request-swype-code

Contrary to `request-swype-code` this RPC method immediately returns a swype
code calculated from block signature of recent block. This operation is always
free of charge since no transaction is generated.

### Parameters

None.

### Response

    {
      "result":
      {
        "reference-block-height": 1900542,
        "swype-id": 5960639,
        "swype-sequence": "*47134376",
        "swype-seed": "98b181ebf3827746a4a22ff18e060c415202b93c434c5dce5113e9e3a3c73c7f"
      }
    }

## /ent/v1/submit-message

This RPC method is used to store message into blockchain and return unique
byte sequence (represented as QR-code to user) that identifies the stored
message.

Similarly to `request-swype-code` method, this method may be called several
times with different sets of parameters. Contrary to `request-swype-code`
all the data needed to display QR-code is returned immediately, and subsequent
calls are needed just to make sure that the message is permanently stored
into blockchain.

### Parameters (submit)

- clientid - numeric user id within the customer's system;
- message - arbitrary text message 

### Parameters (polling)

- txhash - transaction hash returned by the first call.

### Response (submit)

    {
      "result":
      {
        "txhash": "f215f2be2a3fd7daa60c99290f401a102c788002493210dae9795ba7d98b16f2",
        "referenceBlockHeight": 1881912,
        "referenceBlockHash": "953f8f0fe4f850548fc496f9d6f7e25c94049f7891d01f7e1c096582ba69a420",
        "messageSignature": "253ca49fb4c16b45c0bc"
      }
    }

QR-code is generated from the reference block height, the reference block hash
and the message signature.

### Response (polling, transaction is not confirmed)

    {
      "result":
      {
        "confirmations": 0,
        "height": null
      }
    }

### Response (polling, transaction is confirmed)

    {
      "result":
      {
        "confirmations": 2,
        "height": 1881915
      }
    }

## /ent/v1/submit-media-hash

This RPC method is used to submit hash of captured video file into blockchain.
Similarly to `request-swype-code` and `submit-message`  methods, this method
may be called several times.

### Parameters (submit, swype-code obtained using request-swype-code)

- mediahash - SHA-256 hash of video file. No other hash algorithms are
  supported in this version;
- mediahashtype - should always be "SHA-256" for this version;
- clientid - numeric user id within the customer's system;
- referencetxhash - hash of transaction returned by `request-swype-code` RPC
  call used to generate the swype-code for this video file;
- message - optional text message, may be omitted if no message is attached
  to the transaction.

### Parameters (submit, swype-code obtained using fast-request-swype-code)

- mediahash;
- mediahashtype;
- clientid;
- referenceblockheight - reference block height returned by
  `fast-request-swype-code` RPC call used to generate the swype-code for this
  video file;
- message - optional text message, may be omitted if no message is attached
  to the transaction.

### Parameters (submit, without swype-code)

- mediahash;
- mediahashtype;
- clientid;
- noreference (just a parameter name, without a value);
- message - optional text message, may be omitted if no message is attached
  to the transaction.

### Parameters (polling)

- txhash - transaction hash returned by the first call.

### Response (submit)

    {
      "result":
      {
        "txhash": "7b7740bbbc23f56b1246c3b023cbff8726fcc889f180a45a2f3935e60a5272cb"
      }
    }

### Response (polling, transaction is not confirmed)

    {
      "result":
      {
        "confirmations": 0,
        "height": null
      }
    }

### Response (polling, transaction is confirmed)

    {
      "result":
      {
        "confirmations": 2,
        "height": 1881915
      }
    }

## /ent/v1/verify-prover-media
## /ent/v1/verify-clapperboard-media
## /ent/v1/verify-media

All these requests do exactly the same. The actual name doesn't matter. First
two variants are preserved for backward compatibility and may be removed in the
future.

The Software DOES NOT provide any methods for uploading video files. Neither
does The Software remove files in the upload directory. Management of files
in the upload directory is OUT OF SCOPE. Period!

All verify jobs are queued and executed asynchronously in background. When a
job is queued, SHA-256 hash of the file is calculated and returned. Status of
the particular job may be requested using the file hash.

### Parameters (list a job)

- filename - name of the existing file within the upload directory;
- realfilename - original file name if the file was for some reason renamed;
  this "real name" may (or may not) be used to determine file format;

### Parameters (polling)

- filehash - string returned by the first call.

### Response (place job)

    {
      "result":
      {
        "filehash": "a246429354f3caf66095e96387eb539f7658bc60bca2f70190eddce0ad4e9ec2"
      }
    }

### Response (job is finished)

    {
      "result":
      {
        "submitMediaHash":
        {
          "blockTimestamp": 1547131121,
          "blockHash": "ff22f53bcfd87787e8ec4a45ec525e15603cacf37edf3c5e7bb24564de16b2ad",
          "transactionHash": "40e547e7af61f36711f80341cc26f159a1ffe203d869a3e4d9243ab202cbc656",
          "clientId": 100,
          "sender": "NBGICTQQZXWOY64WL5OQ7LKVXK6REMYURYEXTNUD",
          "amount": 1000000,
          "blockHeight": 1974927,
          "message": "",
          "recipient": "NCD47EZ7723UCYQCNHHE3YSKWMWVMM7NLO7ITEST"
        },
        "swype": "*82156616",
        "swypeCodeEndOffset": 94.746,
        "proven": true,
        "va":
        {
          "cutdetect": []
        },
        "reference":
        {
          "sender": "NBGICTQQZXWOY64WL5OQ7LKVXK6REMYURYEXTNUD",
          "recipient": "NB5WGV4EZ6ZTO3DO2O5RH2TWIV6CYILTCK3NTEST",
          "amount": 5000000,
          "blockHeight": 1974925,
          "blockSignature": "ac5d36c749d1bd4d9e46491ee2a3f50d1252aacce5aff00bb8136ffc6d4d3c208181d192a965fde68fd2889f405f5a143f9b7b5c46ecbcbd5be76675d2eab30a",
          "blockTimestamp": 1547130887,
          "transactionHash": "54f409bd70f70c6cfd6928f22bae5adf0d995d294ba1066af1e424eee9c1ace4",
          "blockHash": "b9f12f9ed883c4d918a9e93a8fd0af1957a9dddda4661435e4517b16f6912869"
        },
        "swypeCodeBeginOffset": 82.483,
        "status": "success"
      }
    }

## /local/api/unlock

This RPC method is accessible at the same TCP port as the local web GUI (7898
by default). It accepts the current password (as a plain text) and unlocks the
key store. It's used internally by the local web GUI, but may also be used
by the system administrator to "boot" the server at the system startup.

### Parameters

- password - current password.

### Response (place job)

    {
      "result": null
    }
