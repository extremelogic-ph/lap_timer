# lap_timer
A stop watch like timer using tag names to track time

# Deploy To Nexus

## Used *~/.m2/settings.xml* to support *pom.xml*

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>USER_NAME</username>
      <password>PASSWORD</password>
    </server>
  </servers>
</settings>
```

## Naming

Check the `artifact-id` if that is what you want.

## Command to deploy

```bash
mvn clean deploy -Dmaven.test.skip=true
```

During the deployment process, a popup will ask you to input a passphrase.
Hope your password is 8 characters long for security purposes.

Deployment will take about 8 minutes.

#### Common Errors when deploying

##### IOCTL

```bash
gpg: signing failed: Inappropriate ioctl for device
```

Use the following to fix

```bash
export GPG_TTY=$(tty)
```

##### Keys not in server

Copy the key and

```bash
gpg --list-secret-keys

sec   rsaNNNN YYYY-MM-DD [SC] [expires: YYYY-MM-DD]

      ABCDEFGHIJKLMNOPQRSTUVQXYZ1234567890

uid           [ultimate] First Name Last Name <your@email.com>

ssb   rsaNNNN YYYY-MM-DD [E] [expires: YYYY-MM-DD]
```

send it to a key server. There are other key servers, send it there also.

```bash
gpg --keyserver hkp://keyserver.ubuntu.com --send-keys ABCDEFGHIJKLMNOPQRSTUVQXYZ1234567890
```
