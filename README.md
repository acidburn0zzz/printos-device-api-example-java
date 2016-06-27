# printos-device-api-example-java
Demonstrates the initial provisioning and login of a device into HP PrintOS as well as sending real-time status.

See [https://youtu.be/_D_e7pbWXq4](https://youtu.be/_D_e7pbWXq4) for a short video tutorial of this code.

![Device Overview Screenshot](https://raw.githubusercontent.com/hpinc/printos-device-api-example-java/master/etc/images/device_overview.png)

# Overview
HP PrintOS exposes a set of APIs to allow devices to be created, log in, and send real-time statistics.  Those
APIs are exercised with this example project.  This example allows you to specify your (human) login and password
and create a device of a certain type and model in your PrintOS account, then send in some statistics about it
(current job count, printing status, and time to idle).

Typically, this is intended for device manufacturers that want their devices to be a part of the PrintOS ecosystem.
Having a device "provision" itself (get its own login and password) in this way allows the device to send in real-time
stats and possibly interact with other services in PrintOS.

A PrintOS-enabled device typically has a UI screen that allows the end-user to 'cloud-activate' the device.  This UI screen
consists of fields that allow the user to enter their PrintOS login and password, along with an 'Activate Now' button or something
similar.  When the user enters their login and password, the device first uses that to log in (one time only) to obtain
a PSP admin-capable security token, then that token is used in a header for a subsequent API call to create the device and 
receive back its own set of credentials (device login and password).  The device should securely store these, and then
the human's login/password is no longer needed.

Once the device has its own login/password, these can be used in the `/devices/login` call to log in the device and 
get a device token back.  (The token is really a session ID.).  The device provides this token as a header on all subsequent
API calls when sending real-time status to authenticate itself, handling the cases when its token expires.

## Do I Need To Do This To Accept Jobs?
No.  If all you want is for your device to be able to accept jobs from PrintOS, such as through PrintOS Box or SiteFlow,
it is not necessary for the device to go through the entire provisioning process.  If the device can accept jobs via
hotfolder, you can simply set up a Hotfolder device and configure Box or Siteflow to send to that hotfolder.  It is 
only if you want your device to show real-time stats (job count, printing status, time to completion) that you'll
need to support the provisioning process outlined here.

# Prerequisites
You should already have access to the PrintOS area of the HP Developer Portal ([hp.io](http://hp.io)).  You will also need an
invitation to create an account on our staging stack.  When you request access to PrintOS through [hp.io](http://hp.io), you
should get both of these.

When you accept the e-mail invitation, you'll create a login and password, and you'll set up your own PSP account,
in which you'll be a PSP admin.  PSP admin rights are required in order to manage devices.  You'll need to specify 
these credentials in a config file in the example code.

# Setup
**Required:** Once you download the example code, look for the file called `creds.properties`.  Edit this file and replace the
psp_login and psp_password fields with the login and password you created when you accepted the PrintOS invitation.

**Required:** Examine `settings.properties` and change any settings in there that you need to (e.g. proxy_enabled if
your environment requires it).  

# Running the Example
Once the setup steps are complete, simply run the main() method of `com.hp.printos.deviceapi.example.Main`.  This will
log in as you, create the device, log in as the device, and send statistics about the device (job count, time to
completion, and printing status).  This will create the device in your organization's account.  Once it's there, you
can log in to the integration server at  [https://stg.printopt.org](https://stg.printopt.org) with your PSP admin login 
and password, and go to 'Account'
in the app switcher and go to 'Devices'.  You'll see the device in the list, as well as on the home page along with
the status you supplied.

![Device List Screenshot](https://raw.githubusercontent.com/hpinc/printos-device-api-example-java/master/etc/images/device_list.png)

![Device Tile Screenshot](https://raw.githubusercontent.com/hpinc/printos-device-api-example-java/master/etc/images/device_tile.png)

# Switching to Production
In `settings.properties`, you'll see the following entry:

```
base_url=https://stage.printos.api.hp.com/platform
```

Once your development is done, you can point to production by removing the 'stage', e.g.

```
base_url=https://printos.api.hp.com/platform
```

Note that you will need a separate account invitation to production in order to view devices on that stack, so you'll
also want to update the `psp_login` and `psp_password` in `creds.properties` as well with the new account information.
If you're integrating with the production APIs, you'll want to use the production URL for the site
([https://printos.com](https://printos.com)).

# Best Practices
Typically, PrintOS-enabled devices will have a UI screen on the device itself to allow the user to enter their
credentials to provision the device.  Devices should store their own device login and password securely encrypted
on the device itself.  The user (human) credentials should never be stored on the device and should never be used
except for the initial provisioning of the device into PrintOS.

Once logged in, the device will get its token expiration back from the server and should use that information as a
hint to handle the expiration of the token.  The device will need to gracefully handle the expiration of a session
token and log in again to get a new one.  Do not log in every time you need to send status; use the token you already
have if it's not expired.

Devices should not send status more often than every 1-2 minutes.

Since devices in the field can be hard to get updated, your device code should be very tolerant of all situations
that might arise (servers being down, tokens expiring, etc.)  You code should be capable of handling any response
code from the server.

# Other Things To Know

### Serial Number Collisions
If you try to provision the same device type, model, and serial number twice,
PrintOS (intentionally) replaces the old device with the new one.  This allows a device to re-provision itself
in the case where the device login or password is lost.  However, it will lose any specific changes to that device
the user might have made, so normally you should avoid this unless necessary.

If you try to register the same serial number of a particular device and model and it's already in use by another
organization, the addition is disallowed.

### The List of Device Types and Models
This list is in the database and is currently (intentionally) not programmatically extensible.  In other words, if
you want your device to be provisionable following these instructions, it'll already need to be registered in the
database, have an image in our image repository, etc.  So, we'll need to get it on the list for you.  Contact us
and we can get it added.

### Extending the Real-Time Data Sent
The POST of real-time data in this example only shows the basics (job count, etc.)  We also support the inclusion
of an extendedInformation node in the payload that can be used by our UI to show more extensive stats for more
sophisticated devices.  This is used, for example, to show ink levels, job history and more for HP Latex devices.
This does require us to modify the device UI in PrintOS, so contact us if you're interested in more information
on this and we can possibly work together to provide the best experience for your device.

### Device States
The device state must be one of these values:

```
'DS_ON','DS_OFF','DS_PRINTING','DS_STANDBY','DS_ERROR','DS_RIPPING'
```

Using a different value will cause an error.

### Which Devices Can I Add With This Sample Code?
You should choose a device from the **IHPSPress** or **FinishingDevice** families.  The other device families have some restrictions.  Because LatexPrinter and ScitexPrinter are not indicated as "self-provisionable" devices, they do not return a login and password when you create the device.  (These devices are typically just added from the PrintOS Account application UI.)  Therefore, you do not have credentials required to subsequently log the device in and send statistics.  You can still create it, but you can't do anything interesting with it.

Indigo devices will fail to provision because those have some additional back-end security checks to make sure the serial number of the device matches the correct organization.  The "ThreeDPrinter" family is only there for prototyping and does not have permissions to update statistics, so you'll get 401's if you try to post stats for those.

If you leave the device type and model set to the default in settings.properties, the sample code should work for you.  If you are a device manufacturer and you want to have your device listed, please contact us and we can add it.
