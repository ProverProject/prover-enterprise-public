# Prover Enterprise Backend JSON error codes

## -101 NIS network error

Some network error occured during data interchange with NIS. It may be
unexpected end of stream or broken stream format.

"data" field of the error object contains detailed information about the
error:

- "category" - textual category of the error;
- "code" - numeric error code within the specified category;
- "message" - error message.

Applicable to:

- `get-balance`
- `request-swype-code`
- `submit-media-hash`
- `submit-message`

## -102 NIS HTTP error

HTTP request sent to NIS has ended with HTTP status code other than 200.

"data" field of the error object contains detailed information about the
error:

- "status" - HTTP status code of NIS reply.

Applicable to:

- `get-balance`
- `request-swype-code`
- `submit-media-hash`
- `submit-message`

## -103 Bad NIS reply format

Data returned by NIS has unexpected format (some attributes are missing
or have wrong type). It may also mean that NIS is syncing and can't reply
to HTTP requests correctly.

Applicable to:

- `get-balance`
- `request-swype-code`
- `submit-media-hash`
- `submit-message`

## -201 Key store is not initialized

Key store is uninitialized or corrupted.

Applicable to:

- `get-balance`
- `request-swype-code`
- `submit-media-hash`
- `submit-message`
- `estimate-fee`

## -202 Key store is locked

Key store is ok but it's locked (the private key wasn't decrypted into
memory). In this state RPC requests emitting new transactions can't be
called.

Applicable to:

- `request-swype-code`
- `submit-media-hash`
- `submit-message`

## -301 Index is not up-to-date

Indexing is not finished or can't be performed now.

Applicable to:

- `fast-request-swype-code`
- `request-swype-code`
- `submit-media-hash`
- `submit-message`
- `estimate-fee`

## -401 Insufficient funds

There's no enough tokens or XEM to perform the request.

Applicable to:

- `request-swype-code`
- `submit-media-hash`
- `submit-message`
- `estimate-fee` (this code is returned only if the balance is below the
  minimum of 500,000.000000 PF).

## -402 Transaction announce error

This error is returned if submission of new transaction has failed. "data"
field of the error object contains detailed information about the error:

- "code" - numeric error code (see https://nemproject.github.io/#nemRequestResult)
- "message" textual error message.

Applicable to:

- `request-swype-code`
- `submit-media-hash`
- `submit-message`

## -501 Invalid job token

Token provided to `verify-prover-media` or `verify-clapperboard-media` was
not returned by the previous call to these RPC requests. It may happen also
if the database was re-initialized.

Applicable to:

- `verify-prover-media`
- `verify-clapperboard-media`
