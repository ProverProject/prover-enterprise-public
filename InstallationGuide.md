# Prover Enterprise installation guide

## Set up NEM Node

### Install Java

NEM Node software is written in Java and needs Java Runtime Environment 8.

[Java download page](https://java.com/en/download/manual.jsp)

### Get NEM Node (NIS)

[Download page](https://nem.io/downloads/)

Prover Enterprise was tested with NEM Node v0.6.95 and v0.6.96.

### Configure NEM Node

Set greater `-Xms` and `-Xmx` in `runNis.bat` if you have enough memory. There were issues with default settings, like NEM Node failing to sync. 
`-Xms` is starting memory and `-Xmx` is max memory used by the program.
Recommended settings are `-Xms1G -Xmx3G`

### Run NEM Node

Start `runNis.bat`. It takes several hours to index the database for the first time.

## Set up Prover Enterprise

### Install Prover Enterprise

This document assumes that you already have the Prover Enterprise installer.

Installation is straightforward: next -> next -> finish, no configuration needed.

**Note**: admin rights are required.

### Prover Enterprise is a service

Prover Enterprise runs as a service. It starts automatically after installation and on system startup. To stop it, you can use Windows Task Manager or Windows Services Manager.

The service is named "ProverEnterprise" in Task Manager and "Prover Enterprise" in Services Manager.

### Configure Prover Enterprise

Prover Enterprise works out of the box, but you might want to change some settings.

Configuration file path is ```C:\ProgramData\ProverEnterprise\config.yml```. You need to stop the Prover Enterprise service before editing config and start it again when done.

----

NEM Node address and port.

**Warning**: NEM Node ignores some requests if they don't come from localhost. Prover Enterprise cannot work without them. If you absolutely need to run NEM Node elsewhere, you can use e.g. SSH tunnel.

``` yaml
nemnode:
  address: 127.0.0.1
  port: 7890
```

----

RPC server listens on this address and port.

You can change address to 0.0.0.0 if you want it to accept requests from outside world, but this is not recommended.

``` yaml
rpcserver:
  address: 127.0.0.1
  port: 7899
```

----

Web GUI listens on this address and port.

You can change address to 0.0.0.0 if you want it to accept requests from outside world, but this is not recommended.

``` yaml
webgui:
  address: 127.0.0.1
  port: 7898
```

## Use Prover Enterprise

### Register your license

Open Web GUI in your browser (by default [localhost:7898]), type your desired password twice and click "Send".

This also unlocks your license, so you can use JSON-RPC API.

### Unlock your license

If you restart the service or computer, you need to unlock your license. To do this open Web GUI in your browser, type your password and click "Send".
