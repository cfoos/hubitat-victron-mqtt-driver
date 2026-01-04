import java.math.RoundingMode

metadata {
    definition(name: "Victron Solar Child", namespace: "cfoos", author: "cfoos") {
        capability "PowerMeter"
        capability "CurrentMeter"

    }
}

preferences {
    input name: "currentChangeSensitivity", type: "number", title: "Current Change Sensitivity (1 = 0.01A, 99 = 0.99A)", range: "1..99", defaultValue: 1
    input name: "powerChangeSensitivity", type: "number", title: "Power Change Sensitivity (1 = 0.01W, 99 = 0.99W)", range: "1..99", defaultValue: 1
    input name: "debugLoggingEnabled", type: "bool", title: "Enable debug logging", defaultValue: true
}

def installed() {
    logDebug "Installed Victron Solar Child"
}

def updated() {
    logDebug "Updated Victron Solar Child"
}

def parse(String description) {
    logDebug "Parsing: ${description}"
}

void sendEventIfChanged(String attributeName, String unit, BigDecimal newValue) {
    def lastValue = device.currentValue(attributeName) ? device.currentValue(attributeName).toBigDecimal() : 0.0
    def sensitivity = getSensitivityForAttribute(attributeName)
    def threshold = sensitivity * 0.01

    def formattedValue = newValue.setScale(2, RoundingMode.HALF_UP)

    if (Math.abs(formattedValue - lastValue) >= threshold) {
        switch (attributeName) {
            case "pvVoltage":
                sendEvent(name: "voltage", value: formattedValue.toString(), unit: unit)
                logDebug "Voltage updated to ${formattedValue}${unit}"
                break
            case "pvPower":
                sendEvent(name: "power", value: formattedValue.toString(), unit: unit)
                logDebug "Power updated to ${formattedValue}${unit}"
                break
            case "pvCurrent":
                sendEvent(name: "current", value: formattedValue.toString(), unit: unit)
                logDebug "Current updated to ${formattedValue}${unit}"
                break
            default:
                sendEvent(name: attributeName, value: formattedValue.toString(), unit: unit)
                logDebug "Sent event for ${attributeName}: ${formattedValue}${unit}"
        }
    } else {
        logDebug "${attributeName} change (${formattedValue}) below threshold (${threshold}); no event sent."
    }
}

BigDecimal getSensitivityForAttribute(String attributeName) {
    if (attributeName.toLowerCase().contains("current")) {
        return currentChangeSensitivity ?: 1
    } else if (attributeName.toLowerCase().contains("power")) {
        return powerChangeSensitivity ?: 1
    } else {
        return 1
    }
}

def logDebug(String msg) {
    if (debugLoggingEnabled) {
        log.debug msg
    }
}
