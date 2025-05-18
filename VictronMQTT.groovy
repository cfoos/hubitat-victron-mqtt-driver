/**
 * Hubitat Device Driver
 *
 * MIT License
 *
 * Copyright (c) 2025 Seth Kinast
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.hubitat.app.DeviceWrapper
import com.hubitat.app.ChildDeviceWrapper

metadata {
    definition(
        name: 'Victron MQTT Client',
        namespace: 'cfoos',
        author: 'cfoos',
        importUrl: "https://raw.githubusercontent.com/cfoos/hubitat-victron-mqtt-driver/refs/heads/main/VictronMQTT.groovy"
    )
    {
        capability "Initialize"
        capability "Battery"
        capability "VoltageMeasurement"
        capability "PowerMeter"
        capability "TemperatureMeasurement"
        capability "CurrentMeter"
        attribute 'batteryAmperage', 'number'
        attribute 'batteryTemperature', 'number'
        attribute 'batteryPower', 'number'
        attribute 'batterySOC', 'number'
        attribute 'batteryVoltage', 'number'
        attribute 'acInSource', 'string'
        attribute 'acL1GridInPower', 'number'
        attribute 'acL2GridInPower', 'number'
        attribute 'acL3GridInPower', 'number'
        attribute 'acL1ConsumptionPower', 'number'
        attribute 'acL2ConsumptionPower', 'number'
        attribute 'acL3ConsumptionPower', 'number'
        attribute 'pvPower', 'number'
        attribute 'inverterPower', 'number'
    }
}

preferences {
    section('MQTT') {
        input name: 'victronIP', type: 'string', title: 'Victron IP Address', required: true
        input name: 'victronPort', type: 'string', title: 'Victron Port', required: true, defaultValue: 1883
        input name: 'victronUsername', type: 'string', title: 'VRM Username (email address) "Only needed for web access"'
        input name: 'vrmID', type: 'string', title: 'VRM ID'
        input name: 'vrmAccessToken', type: 'string', title: 'VRM Access Token "Only needed for web access"'
        input name: 'vrmSSL', type: 'bool', title: 'Enable ssl "Only needed for web access"', defaultValue: false
    }
    section('Advanced') {
        input name: 'debugLoggingEnabled', type: 'bool', title: 'Enable debug logging', defaultValue: true
    }
}

String vrmCert = """-----BEGIN CERTIFICATE-----
MIIECTCCAvGgAwIBAgIJAM+t3iC8ybEHMA0GCSqGSIb3DQEBCwUAMIGZMQswCQYD
VQQGEwJOTDESMBAGA1UECAwJR3JvbmluZ2VuMRIwEAYDVQQHDAlHcm9uaW5nZW4x
HDAaBgNVBAoME1ZpY3Ryb24gRW5lcmd5IEIuVi4xIzAhBgNVBAsMGkNDR1ggQ2Vy
dGlmaWNhdGUgQXV0aG9yaXR5MR8wHQYJKoZIhvcNAQkBFhBzeXNhZG1pbkB5dGVj
Lm5sMCAXDTE0MDkxNzExNTQxOVoYDzIxMTQwODI0MTE1NDE5WjCBmTELMAkGA1UE
BhMCTkwxEjAQBgNVBAgMCUdyb25pbmdlbjESMBAGA1UEBwwJR3JvbmluZ2VuMRww
GgYDVQQKDBNWaWN0cm9uIEVuZXJneSBCLlYuMSMwIQYDVQQLDBpDQ0dYIENlcnRp
ZmljYXRlIEF1dGhvcml0eTEfMB0GCSqGSIb3DQEJARYQc3lzYWRtaW5AeXRlYy5u
bDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKVdbAUAElbX+Sh0FATX
yhlJ6zqYMHbqCXawgsOe09zHynDCT4GTXuSuoH2kR/1jE8zvWNLHORXa/eahzWJP
V4WpXuYsFEyU3r8hxA6y+SR06IT7WHdfN6LXN+qt5KLQbmQLxeb1zElMKW4io/WE
N+SWpo5dklXAS6vnq+VRTNwRYnPOUIXKduhvTQp6hEHnLBjYC/Ot8SkC8KtL88cW
pH6d7UmeW3333/vNMEMOTLWlOWrR30P6R+gTjbvzasaB6tlcYqW+jO1YDlBwhSEV
4As4ziQysuy4qvm41KY/o4Q6P6npsh8MaZuRmi/UTxU2DHAbs/on7qaRi6IkVgvg
o6kCAwEAAaNQME4wHQYDVR0OBBYEFPjmM5NYXMw7Wc/TgbLtwPnMAfewMB8GA1Ud
IwQYMBaAFPjmM5NYXMw7Wc/TgbLtwPnMAfewMAwGA1UdEwQFMAMBAf8wDQYJKoZI
hvcNAQELBQADggEBAEFTeGcmxzzXJIfgUrfKLki+hi2mR9g7qomvw6IB1JQHefIw
iKXe14gdp0ytjYL6QoTeEbS2A8VI2FvSbusAzn0JqXdZI+Gwt/CuH0XH40QHpaQ5
UAB5d7EGvbB2op7AA/IyR5TwF/cDb1fRbTaTmwDOIo3kuFGEyNCc+PFrN2MvtPHn
hHH7fo7joY7mUKdP573bJXFsLwZxlqiycJreroLPFqYwgChaMTStQ71rP5i1eGtg
ealQ7kPVtlHmX89tCkfkK77ojm48qgl4gwsI01SikstaPP9fr4ck+U/qIKhSg+Bg
nc9OImY9ubQxe+/GQP4KFme2PPqthEWys7ut2HM=
-----END CERTIFICATE-----"""

void initialize() {
    state.remove('connectDelay')
    connect()
    subscribe()
    topic = "R/${vrmID}/keepalive"
    interfaces.mqtt.publish("${topic}", "1")
    runEvery1Minute(keepalive)

    if (debugLoggingEnabled) {
        runIn(3600, disableDebugLogging)
    }
}

void updated() {
    initialize()
}

void parse(String event) {
    def message = interfaces.mqtt.parseMessage(event)
    logDebug message.toString()

    if (message.topic == "N/${vrmID}/system/0/Dc/Battery/Soc") {
        def TempData = parseJson( message.payload )
        sendEvent(name: 'batterySOC', value: Math.round(TempData.value * 100)/100, unit:"%")
    }
    if (message.topic == "N/${vrmID}/system/0/Dc/Battery/Voltage") {
        def TempData = parseJson( message.payload )
        sendEvent(name: 'batteryVoltage', value: Math.round(TempData.value * 100)/100, unit:"V")
    }
    if (message.topic == "N/${vrmID}/system/0/Dc/Battery/Power") {
        def TempData = parseJson( message.payload )
        sendEvent(name: 'batteryPower', value: Math.round(TempData.value * 100)/100, unit:"W")
    }
    if (message.topic == "N/${vrmID}/system/0/Dc/Battery/Temperature") {
        def TempData = parseJson( message.payload )
        sendEvent(name: 'batteryTemperature', value: Math.round(TempData.value * 100)/100, unit:"°C")
    }
    if (message.topic == "N/${vrmID}/system/0/Dc/Battery/Current") {
        def TempData = parseJson( message.payload )
        sendEvent(name: 'batteryAmperage', value: Math.round(TempData.value * 100)/100, unit:"A")
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/ActiveIn/Source") {
        def TempData = parseJson( message.payload )
        logDebug 'AC source is '+ TempData
        switch (TempData.value){
            case "0":
                sendEvent(name: 'acInSource', value: "Not Available")
                break
            case "1":
                sendEvent(name: 'acInSource', value: "Grid")
                break
            case "2":
                sendEvent(name: 'acInSource', value: "Generator")
                break
            case "3":
                sendEvent(name: 'acInSource', value: "Shore")
                break
            case "240":
                sendEvent(name: 'acInSource', value: "Inverting/Island mode")
                break
            default:
                sendEvent(name: 'acInSource', value: "Undetected")
                break
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/Grid/L1/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'acL1GridInPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/Grid/L2/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'acL2GridInPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/Grid/L3/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'acL3GridInPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/ConsumptionOnOutput/L1/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'acL1ConsumptionPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/ConsumptionOnOutput/L2/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'acL2ConsumptionPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Ac/ConsumptionOnOutput/L3/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'acL2ConsumptionPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Dc/Pv/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'pvPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
    if (message.topic == "N/${vrmID}/system/0/Dc/InverterCharger/Power") {
        def TempData = parseJson( message.payload )
        if ( TempData.value != null ){
            sendEvent(name: 'inverterPower', value: Math.round(TempData.value * 100)/100, unit:"W")
        }
    }
}

/* MQTT */

void connect() {
    try {
        if( !vrmSSL ){
            logDebug "Connecting to Victron broker at ${getMQTTConnectURI()}, \"victron_mqtt_client_${device.id}\", victronUsername, vrmAccessToken"
            interfaces.mqtt.connect(getMQTTConnectURI(), "victron_mqtt_client_${device.id}", victronUsername, vrmAccessToken)
        } else {
            logDebug "Connecting to Victron broker at ${getMQTTConnectURI()}, \"victron_mqtt_client_${device.id}\", victronUsername, vrmAccessToken, privateKey: vrmCert, caCertificate: vrmCert, clientCertificate: vrmCert, ignoreSSLIssues: true}"
            interfaces.mqtt.connect(getMQTTConnectURI(), "victron_mqtt_client_${device.id}", victronUsername, vrmAccessToken, privateKey: vrmCert, caCertificate: vrmCert, clientCertificate: vrmCert, ignoreSSLIssues: true)
        }
    } catch (e) {
        log.error "Error connecting to Victron broker: ${e.message}"
        reconnect()
    }
}

void subscribe() {
    if (interfaces.mqtt.isConnected()) {
        topic = "N/${vrmID}/system/0/Dc/Battery/Soc"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Dc/Battery/Voltage"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Dc/Battery/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Dc/Battery/Temperature"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Dc/Battery/Current"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/Grid/L1/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/Grid/L2/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/Grid/L3/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/ActiveIn/Source"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/ConsumptionOnOutput/L1/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/ConsumptionOnOutput/L2/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Ac/ConsumptionOnOutput/L3/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Dc/Pv/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
        topic = "N/${vrmID}/system/0/Dc/InverterCharger/Power"
        logDebug 'Subscribing to ' + topic
        interfaces.mqtt.subscribe(topic)
    }
}


void keepalive() {
    topic = "R/${vrmID}/keepalive"
    options = '{ "keepalive-options" : ["suppress-republish"] }'
    interfaces.mqtt.publish("${topic}", "${options}")
}

void reconnect() {
    state.connectDelay = state.connectDelay ?: 0
    state.connectDelay = Math.min(state.connectDelay + 1, 5)

    runIn(state.connectDelay * 60, connect)
}


void mqttClientStatus(String status) {
    logDebug status
    state.status = status
    if (status.startsWith('Error')) {
        try {
            interfaces.mqtt.disconnect()
        } finally { reconnect() }
    } else {
        state.remove('connectDelay')
        runIn(1, subscribe)
    }
}


/* Helpers */

String getMQTTConnectURI() {
    if( !vrmSSL ){
        "tcp://${victronIP}:${victronPort}"
    } else {
        "ssl://${victronIP}:${victronPort}"
    }
}

String getTopic(String topicSuffix) {
    (topicPrefix == null || topicPrefix == '') ? topicSuffix : topicPrefix + '/' + topicSuffix
}

void disableDebugLogging() {
    device.updateSetting('debugLoggingEnabled', [value: false, type: 'bool'])
}

void logDebug(String msg) {
    if (debugLoggingEnabled) {
        log.debug msg
    }
}

