package org.devops.kubernetes

def deploymentYamlBuilder(String yamlFile='deployment.yaml', safelyExit=Config.data.safelyExit) {
    def deployment = [
        apiVersion: 'apps/v1',
        kind: 'Deployment',
        metadata: [
            name: '$APP_NAME',
            namespace: '$NAMESPACE',
            labels: [
                app: 'kubesphere',
                component: '$APP_NAME',
                tier: 'backend'
            ]
        ],
        spec: [
            progressDeadlineSeconds: 600,
            replicas: Config.data.replicas,
            selector: [
                matchLabels: [
                    app: 'kubesphere',
                    component: '$APP_NAME',
                    tier: 'backend'
                ]
            ],
            minReadySeconds: 5,
            strategy: [
                rollingUpdate: [
                    maxSurge: 1,
                    maxUnavailable: 0
                ],
                type: 'RollingUpdate'
            ],
            template: [
                metadata: [
                    labels: [
                        app: 'kubesphere',
                        component: '$APP_NAME',
                        tier: 'backend'
                    ]
                ],
                spec: [
                    affinity: [
                        podAntiAffinity: [
                            preferredDuringSchedulingIgnoredDuringExecution: [[
                                weight: 1,
                                podAffinityTerm: [
                                    topologyKey: 'kubernetes.io/hostname',
                                    labelSelector: [
                                        matchExpressions: [[
                                            key: 'app',
                                            operator: 'In',
                                            values: [
                                            '$APP_NAME'
                                            ]
                                        ]]    
                                    ]
                                ]
                            ]]
                        ]
                    ],
                    imagePullSecrets: [[
                        name: '$IMAGEPULLSECRETS'
                    ]],
                    containers: [[
                        name: '$APP_NAME',
                        image: '$REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$BRANCH_NAME-$BUILD_NUMBER',
                        imagePullPolicy: 'IfNotPresent',
                        ports: [[
                            containerPort: 8080,
                            protocol: 'TCP'
                        ]],
                        env: [[
                            name: 'CACHE_IGNORE',
                            value: 'js|html'
                        ],[
                            name: 'CACHE_PUBLIC_EXPIRATION',
                            value: '3d'
                        ]],
                        readinessProbe: [
                            httpGet: [
                                path: '/',
                                port: 8080
                            ],
                            initialDelaySeconds: 15,
                            timeoutSeconds: 3,
                            periodSeconds: 30
                        ],
                        livenessProbe: [
                            httpGet: [
                                path: '/',
                                port: 8080
                            ],
                            initialDelaySeconds: 10,
                            timeoutSeconds: 3,
                            periodSeconds: 30
                        ],
                        resources: [
                            limits: [
                                cpu: '300m',
                                memory: '600Mi'
                            ],
                            requests: [
                                cpu: '100m',
                                memory: '100Mi'
                            ]
                        ],
                        lifecycle: [
                            preStop: [
                                exec: [
                                    command: [
                                        '/bin/sh',
                                        '-c',
                                        'sleep 5'
                                    ]
                                ]
                            ]
                        ],
                        securityContext: [
                            readOnlyRootFilesystem: true,
                            runAsUser: 1000,
                            runAsGroup: 1000
                        ],
                        terminationMessagePath: '/dev/termination-log',
                        terminationMessagePolicy: 'File',
                        volumeMounts: [[
                            name: 'tmp',
                            mountPath: '/tmp'
                        ]]
                    ]],
                    dnsPolicy: 'ClusterFirst',
                    restartPolicy: 'Always',
                    terminationGracePeriodSeconds: 30,
                    volumes: [[
                        name: 'tmp',
                        persistentVolumeClaim: [
                            claimName: '$APP_NAME'
                        ]
                    ]]
                ]
            ]
        ]
    ]

    if ( env.BRANCH_NAME == 'master' ) {
        yaml_file = env.WORKSPACE + "/" + yamlFile
        writeYaml file: yaml_file, data: deployment, overwrite: true
    } else if ( env.BRANCH_NAME == 'dev' ) {
        deployment.spec.template.spec.containers.resources = [
            limits: [
                cpu: '300m',
                memory: '600Mi'
            ],
            requests: [
                cpu: '100m',
                memory: '100Mi'
            ]
        ]
    }   else {
        deployment.spec.template.spec.containers.resources = [
            limits: [
                cpu: '300m',
                memory: '600Mi'
            ],
            requests: [
                cpu: '100m',
                memory: '100Mi'
            ]
        ]
    }

    yaml_file = env.WORKSPACE + "/" + yamlFile
    writeYaml file: yaml_file, data: deployment, overwrite: true
}

def serviceYamlBuilder(String yamlFile='service.yaml', int serviceNodePort=0) {
        def service = [
            apiVersion: 'v1',
            kind: 'Service',
            metadata: [
                name: '$APP_NAME',
                namespace: '$NAMESPACE',
                labels: [
                    app: 'kubesphere',
                    component: '$APP_NAME'
                ]
            ],
            spec: [
                selector: [
                    app: 'kubesphere',
                    component: '$APP_NAME',
                    tier: 'backend'
                ],
                sessionAffinity: 'None',
                type: 'NodePort',
                ports: [[
                    name: 'http',
                    port: 8080,
                    protocol: 'TCP',
                    targetPort: 8080,
                    nodePort: serviceNodePort
                ]]
            ]
        ]

        if (serviceNodePort == 0) {
            service.spec.type = 'ClusterIP'         
            service.spec.ports = [[name: 'http',
                port: 8080,
                protocol: 'TCP',
                targetPort: 8080]]
        }

        yaml_file = env.WORKSPACE + "/" + yamlFile
        writeYaml file: yaml_file, data: service, overwrite: true
}

def pvcYamlBuilder(String yamlFile='pvc.yaml') {
    def pvc = [
        apiVersion: 'v1',
        kind: 'PersistentVolumeClaim',
        metadata: [
            name: '$APP_NAME',
            namespace: '$NAMESPACE',
        ],
        spec: [
            accessModes: [
                'ReadWriteOnce'
            ],
            resources: [
                requests: [
                    storage: '50Gi',
                ]
            ],
            storageClassName: 'managed-nfs-storage'
        ]
    ]

    yaml_file = env.WORKSPACE + "/" + yamlFile
    writeYaml file: yaml_file, data: pvc, overwrite: true
}