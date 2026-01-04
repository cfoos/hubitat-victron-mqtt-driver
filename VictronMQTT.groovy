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

metadata {
    definition(
        name: 'Victron MQTT Client',
        namespace: 'cfoos',
        author: 'cfoos',
        importUrl: "https://raw.githubusercontent.com/cfoos/hubitat-victron-mqtt-driver/refs/heads/main/VictronMQTT.groovy"
    )
    {
        capability "Initialize"

        attribute 'acInSource', 'string'
        attribute 'acL1GridInPower', 'number'
        attribute 'acL2GridInPower', 'number'
        attribute 'acL3GridInPower', 'number'
        attribute 'acL1GridInCurrent', 'number'
        attribute 'acL2GridInCurrent', 'number'
        attribute 'acL3GridInCurrent', 'number'
        attribute 'acL1ConsumptionPower', 'number'
        attribute 'acL2ConsumptionPower', 'number'
        attribute 'acL3ConsumptionPower', 'number'
        attribute 'acL1ConsumptionCurrent', 'number'
        attribute 'acL2ConsumptionCurrent', 'number'
        attribute 'acL3ConsumptionCurrent', 'number'
        attribute 'pvPower', 'number'
        attribute 'pvCurrent', 'number'
        attribute 'inverterPower', 'number'
        attribute 'inverterCurrent', 'number'
        attribute 'systemState', 'string'
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
    section('Measurement Sensitivity') {
        input name: 'voltageChangeSensitivity', type: 'number', title: 'Voltage Change Sensitivity (1 = 0.01V, 99 = 0.99V)', range: '1..99', defaultValue: 1
        input name: 'currentChangeSensitivity', type: 'number', title: 'Current Change Sensitivity (1 = 0.01A, 99 = 0.99A)', range: '1..99', defaultValue: 1
        input name: 'powerChangeSensitivity', type: 'number', title: 'Power Change Sensitivity (1 = 0.01W, 99 = 0.99W)', range: '1..99', defaultValue: 1
    }
    section('Child Devices') {
        input name: 'enableBatteryChild', type: 'bool', title: 'Enable Battery Child Device', defaultValue: false
        input name: 'enableSolarChild', type: 'bool', title: 'Enable Solar Child Device', defaultValue: false
        input name: 'enableGridChild', type: 'bool', title: 'Enable Grid Child Device', defaultValue: false
        input name: 'enableDcChild', type: 'bool', title: 'Enable DC Child Device', defaultValue: false
    }
}

void createDcChild() {
    def dni = "${device.deviceNetworkId}-DC"
    if (!getChildDevice(dni)) {
        addChildDevice("cfoos", "Victron DC Child", dni, [label: "${device.displayName} DC", isComponent: true])
        logDebug "Created DC child device"
    }
}

void updateDcChild(String attribute, def value, String unit = null) {
    def dni = "${device.deviceNetworkId}-DC"
    def child = getChildDevice(dni)
    if (child) {
        logDebug "Sending to DC child: ${attribute} = ${value}${unit ? " ${unit}" : ""}"
        child.sendEventIfChanged(attribute, unit, value)
    } else {
        log.warn "DC child device not found"
    }
}

void createGridChild() {
    def dni = "${device.deviceNetworkId}-Grid"
    if (!getChildDevice(dni)) {
        addChildDevice("cfoos", "Victron Grid Child", dni, [label: "${device.displayName} Grid", isComponent: true])
        logDebug "Created Grid child device"
    }
}

void updateGridChild(String attribute, def value, String unit = null) {
    def dni = "${device.deviceNetworkId}-Grid"
    def child = getChildDevice(dni)
    if (child) {
        logDebug "Sending to Grid child: ${attribute} = ${value}${unit ? " ${unit}" : ""}"
        child.sendEventIfChanged(attribute, unit, value)
    } else {
        log.warn "Grid child device not found"
    }
}

void createBatteryChild() {
    def dni = "${device.deviceNetworkId}-Battery"
    if (!getChildDevice(dni)) {
        addChildDevice("cfoos", "Victron Battery Child", dni, [label: "${device.displayName} Battery", isComponent: true])
        logDebug "Created Battery child device"
    }
}

void updateBatteryChild(String attribute, def value, String unit = null) {
    def dni = "${device.deviceNetworkId}-Battery"
    def child = getChildDevice(dni)
    if (child) {
        logDebug "Sending to battery child: ${attribute} = ${value}${unit ? " ${unit}" : ""}"
        child.sendEventIfChanged(attribute, unit, value)
    } else {
        log.warn "Battery child device not found"
    }
}

void createSolarChild() {
    def dni = "${device.deviceNetworkId}-Solar"
    if (!getChildDevice(dni)) {
        addChildDevice("cfoos", "Victron Solar Child", dni, [label: "${device.displayName} Solar", isComponent: true])
        logDebug "Created Solar child device"
    }
}

void updateSolarChild(String attribute, def value, String unit = null) {
    def dni = "${device.deviceNetworkId}-Solar"
    def child = getChildDevice(dni)
    if (child) {
        logDebug "Sending to Solar child: ${attribute} = ${value}${unit ? " ${unit}" : ""}"
        child.sendEventIfChanged(attribute, unit, value)
    } else {
        log.warn "Solar child device not found"
    }
}

void initialize() {
    if (enableDcChild) {
        createDcChild()
    } else {
        deleteChildDeviceIfExists("${device.deviceNetworkId}-DC")
    }

    if (enableBatteryChild) {
        createBatteryChild()
    } else {
        deleteChildDeviceIfExists("${device.deviceNetworkId}-Battery")
    }

    if (enableGridChild) {
        createGridChild()
    } else {
        deleteChildDeviceIfExists("${device.deviceNetworkId}-Grid")
    }

    if (enableSolarChild) {
        createSolarChild()
    } else {
        deleteChildDeviceIfExists("${device.deviceNetworkId}-Solar")
    }

    connect()
    subscribe()
    def topic = "R/${vrmID}/keepalive"
    interfaces.mqtt.publish("${topic}", "1")
    runEvery1Minute(keepalive)

    if (debugLoggingEnabled) {
        runIn(3600, disableDebugLogging)
    }
}

void updated() {
    initialize()
}

void connect() {
    try {
        def uri = getMQTTConnectURI()
        def clientId = "victron_mqtt_client_${device.id}"
        if (!vrmSSL) {
            logDebug "Connecting to Victron broker at ${uri}, ${clientId}, ${victronUsername}, ${vrmAccessToken}"
            interfaces.mqtt.connect(uri, clientId, victronUsername, vrmAccessToken)
        } else {
            logDebug "Connecting to Victron broker at ${uri}, ${clientId}, with SSL"
            interfaces.mqtt.connect(uri, clientId, victronUsername, vrmAccessToken,
                privateKey: vrmCert, caCertificate: vrmCert, clientCertificate: vrmCert, ignoreSSLIssues: true)
        }
    } catch (e) {
        log.error "Error connecting to Victron broker: ${e.message}"
        reconnect()
    }
}

void keepalive() {
    def topic = "R/${vrmID}/keepalive"
    def options = '{ "keepalive-options" : ["suppress-republish"] }'
    interfaces.mqtt.publish(topic, options)
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
        } finally {
            reconnect()
        }
    } else {
        state.remove('connectDelay')
        runIn(1, subscribe)
    }
}

void subscribe() {
    if (!interfaces.mqtt.isConnected()) return

    def topicSuffixes = [
        "Dc/Battery/Soc", "Dc/Battery/Voltage", "Dc/Battery/Power", "Dc/Battery/Temperature", "Dc/Battery/Current",
        "Dc/Battery/TimeToGo", "Ac/ActiveIn/Source",
        "Ac/Grid/L1/Power", "Ac/Grid/L2/Power", "Ac/Grid/L3/Power",
        "Ac/Grid/L1/Current", "Ac/Grid/L2/Current", "Ac/Grid/L3/Current",
        "Ac/ConsumptionOnOutput/L1/Power", "Ac/ConsumptionOnOutput/L2/Power", "Ac/ConsumptionOnOutput/L3/Power",
        "Ac/ConsumptionOnOutput/L1/Current", "Ac/ConsumptionOnOutput/L2/Current", "Ac/ConsumptionOnOutput/L3/Current",
        "Dc/Pv/Power", "Dc/Pv/Current",
        "Dc/InverterCharger/Power", "Dc/InverterCharger/Current",
        "Dc/System/Power", "Dc/System/Current",
        "SystemState/State"
    ]

    topicSuffixes.each { suffix ->
        def fullTopic = "N/${vrmID}/system/0/${suffix}"
        logDebug "Subscribing to ${fullTopic}"
        interfaces.mqtt.subscribe(fullTopic)
    }
}

void parse(String event) {
    def message = interfaces.mqtt.parseMessage(event)
    logDebug message.toString()

    def topicMap = [
        "Dc/Battery/Soc"                      : [attr: "batterySOC", unit: "%"],
        "Dc/Battery/Voltage"                  : [attr: "batteryVoltage", unit: "V"],
        "Dc/Battery/Power"                    : [attr: "batteryPower", unit: "W"],
        "Dc/Battery/Temperature"              : [attr: "batteryTemperature", unit: "C"],
        "Dc/Battery/Current"                  : [attr: "batteryCurrent", unit: "A"],
        "Dc/Battery/TimeToGo"                 : [attr: "batteryTimeToGo", unit: ""],
        "Ac/ActiveIn/Source"                  : [attr: "acInSource", unit: ""],
        "Ac/Grid/L1/Power"                    : [attr: "acL1GridInPower", unit: "W"],
        "Ac/Grid/L2/Power"                    : [attr: "acL2GridInPower", unit: "W"],
        "Ac/Grid/L3/Power"                    : [attr: "acL3GridInPower", unit: "W"],
        "Ac/Grid/L1/Current"                  : [attr: "acL1GridInCurrent", unit: "A"],
        "Ac/Grid/L2/Current"                  : [attr: "acL2GridInCurrent", unit: "A"],
        "Ac/Grid/L3/Current"                  : [attr: "acL3GridInCurrent", unit: "A"],
        "Ac/ConsumptionOnOutput/L1/Power"     : [attr: "acL1ConsumptionPower", unit: "W"],
        "Ac/ConsumptionOnOutput/L2/Power"     : [attr: "acL2ConsumptionPower", unit: "W"],
        "Ac/ConsumptionOnOutput/L3/Power"     : [attr: "acL3ConsumptionPower", unit: "W"],
        "Ac/ConsumptionOnOutput/L1/Current"   : [attr: "acL1ConsumptionCurrent", unit: "A"],
        "Ac/ConsumptionOnOutput/L2/Current"   : [attr: "acL2ConsumptionCurrent", unit: "A"],
        "Ac/ConsumptionOnOutput/L3/Current"   : [attr: "acL3ConsumptionCurrent", unit: "A"],
        "Dc/Pv/Power"                         : [attr: "pvPower", unit: "W"],
        "Dc/Pv/Current"                       : [attr: "pvCurrent", unit: "A"],
        "Dc/InverterCharger/Power"           : [attr: "inverterPower", unit: "W"],
        "Dc/InverterCharger/Current"         : [attr: "inverterCurrent", unit: "A"],
        "Dc/System/Power"                     : [attr: "dcPower", unit: "W"],
        "Dc/System/Current"                   : [attr: "dcCurrent", unit: "A"],
        "SystemState/State"                   : [attr: "systemState", unit: ""]
    ]

    topicMap.each { suffix, config ->
        def fullTopic = "N/${vrmID}/system/0/${suffix}"
        if (message.topic == fullTopic) {
            def TempData = parseJson(message.payload)

            if (config.attr == "batteryTimeToGo") {
                def value = TempData?.value != null ? Math.round(TempData.value) : "unknown"
                sendEvent(name: config.attr, value: value)
            } else if (config.attr == "acInSource") {
                def sourceMap = [
                    "0"   : "Not Available",
                    "1"   : "Grid",
                    "2"   : "Generator",
                    "3"   : "Shore",
                    "240" : "Inverting/Island mode"
                ]
                def value = sourceMap.get(TempData?.value?.toString(), "Undetected")
                logDebug "AC source is ${value}"
                sendEvent(name: config.attr, value: value)
            } else if (config.attr == "systemState") {
                def stateMap = [
                    "0"   : "Off",
                    "1"   : "Low power",
                    "2"   : "VE.Bus Fault condition",
                    "3"   : "Bulk charging",
                    "4"   : "Absorption charging",
                    "5"   : "Float charging",
                    "6"   : "Storage mode",
                    "7"   : "Equalisation charging",
                    "8"   : "Passthru",
                    "9"   : "Inverting",
                    "10"  : "Assisting",
                    "244" : "Battery Sustain (Prefer Renewable Energy)",
                    "252" : "External control",
                    "256" : "Discharging",
                    "257" : "Sustain (ESS dynamic cut-off)",
                    "258" : "Recharge",
                    "259" : "Scheduled recharge"
                ]
                def value = stateMap.get(TempData?.value?.toString(), "Undetected")
                logDebug "System state is ${value}"
                sendEvent(name: config.attr, value: value)
            } else {
                if (TempData?.value != null) {
                    def newValue = TempData.value
                    if (enableBatteryChild) {
                        def batteryAttributes = [
                            "batterySOC", "batteryVoltage", "batteryPower",
                            "batteryTemperature", "batteryCurrent", "batteryTimeToGo"
                        ]
                        if (batteryAttributes.contains(config.attr)) {
                            updateBatteryChild(config.attr, newValue, config.unit)
                        }
                    }
                    if (enableDcChild) {
                        def dcAttributes = [
                            "dcPower", "dcCurrent"
                        ]
                        if (dcAttributes.contains(config.attr)) {
                            updateDcChild(config.attr, newValue, config.unit)
                        }
                    }
                    if (enableSolarChild) {
                        def solarAttributes = [
                            "pvPower", "pvCurrent"
                        ]
                        if (solarAttributes.contains(config.attr)) {
                            updateSolarChild(config.attr, newValue, config.unit)
                        }
                    }
                    if (enableGridChild) {
                        def gridAttributes = [
                            "acL1GridInPower", "acL1GridInCurrent"
                        ]
                        if (gridAttributes.contains(config.attr)) {
                            updateGridChild(config.attr, newValue, config.unit)
                        }
                    }
                }
            }
        }
    }
}


/* Helpers */

String getMQTTConnectURI() {
    return !vrmSSL ? "tcp://${victronIP}:${victronPort}" : "ssl://${victronIP}:${victronPort}"
}

void disableDebugLogging() {
    device.updateSetting('debugLoggingEnabled', [value: false, type: 'bool'])
}

void logDebug(String msg) {
    if (debugLoggingEnabled) {
        log.debug msg
    }
}

void sendEventIfChanged(String attributeName, String unit, BigDecimal newValue) {
    def lastValue = device.currentValue(attributeName) ?: 0.0
    def sensitivity = getSensitivityForAttribute(attributeName)
    def threshold = sensitivity * 0.01

    if (Math.abs(newValue - lastValue) >= threshold) {
        sendEvent(name: attributeName, value: newValue, unit: unit)
    } else {
        logDebug "${attributeName} change (${newValue}) below threshold (${threshold}); no event sent."
    }
}

BigDecimal getSensitivityForAttribute(String attributeName) {
    def name = attributeName.toLowerCase()
    if (name.contains("voltage")) {
        return voltageChangeSensitivity ?: 1
    } else if (name.contains("current")) {
        return currentChangeSensitivity ?: 1
    } else if (name.contains("power")) {
        return powerChangeSensitivity ?: 1
    } else {
        return 1
    }
}

void deleteChildDeviceIfExists(String dni) {
    def child = getChildDevice(dni)
    if (child) {
        deleteChildDevice(dni)
        logDebug "Removed child device: ${dni}"
    }
}
