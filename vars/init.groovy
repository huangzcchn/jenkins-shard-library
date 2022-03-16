def Config(Map args=[:]) {
    defaultSettings()
    
    def private s = Config.settings
    Config.data = s + args
}

def defaultSettings() {
    Config.settings = [
        serviceNodePort: 0,
        safelyExit: false,
        replicas: 1    
    ]
    return Config.settings
}

return this