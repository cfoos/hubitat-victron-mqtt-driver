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
    )
    {
        capability "Initialize"
        capability "Battery"
        attribute 'battery', 'number'
        attribute 'lastUpdated', 'date'

    }
}

preferences {
    section('MQTT') {
        input name: 'victronIP', type: 'string', title: 'Victron IP Address', required: true
        input name: 'victronPort', type: 'string', title: 'Victron Port', required: true, defaultValue: 1883
        input name: 'victronUsername', type: 'string', title: 'VRM Username (email address) "Only needed for web access"'
        input name: 'vrmID', type: 'string', title: 'VRM ID'
        input name: 'vrmAccessToken', type: 'string', title: 'VRM Access Token "Only needed for web access"'
        
    }
    section('Advanced') {
        input name: 'debugLoggingEnabled', type: 'bool', title: 'Enable debug logging', defaultValue: true
    }
}

void initialize() {
    state.remove('connectDelay')
    connect()
    subscribe()

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
    logDebug message.topic

    if (message.topic == "N/${vrmID}/system/0/Dc/Battery/Soc") {
        def TempData = parseJson( message.payload )
        sendEvent(name: 'battery', value: TempData.value, unit:"%")
    }
}

/* MQTT */

void connect() {
    try {
        logDebug "Connecting to Victron broker at ${victronIP}:${victronPort}"
        interfaces.mqtt.connect(getMQTTConnectURI(), "victron_mqtt_client_${device.id}", victronUsername, vrmAccessToken)
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
    }
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
    "tcp://${victronIP}:${victronPort}"
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

