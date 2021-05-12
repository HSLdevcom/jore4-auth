package fi.hsl.jore4.auth.spring

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.Resource

/**
 * Component for inspecting that Spring services using @Transactional or @PreAuthorize annotations
 * have proxies generated for them. Application startup fails if any services that violate
 * this constraint exist.
 */
@Component
open class ServiceProxyVerifier {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ServiceProxyVerifier::class.java)
    }

    @Resource
    private val applicationContext: ApplicationContext? = null

    @EventListener(ContextRefreshedEvent::class)
    fun inspectServices() {
        val beans = applicationContext!!.beanDefinitionNames
        var inspectionSuccessful = true

        for (name in beans) {
            val bean = applicationContext.getBean(name)

            if (isService(bean)) {
                if (!bean.javaClass.name.contains("$")) {
                    val serviceClass = bean.javaClass

                    if (!inspectAnnotations(name, serviceClass)) {
                        inspectionSuccessful = false
                    }
                }
            }
        }

        LOGGER.debug("Inspection done.")

        if (!inspectionSuccessful) {
            LOGGER.error("Service inspection failed.")
            throw IllegalStateException("Errors in service proxy generation configuration.")
        }
    }

    private fun inspectAnnotations(name: String, serviceClass: Class<*>): Boolean {
        var currentServiceClass = serviceClass
        var inspectionSuccessful = true

        LOGGER.debug("Inspect service {}", name)
        do {
            LOGGER.debug("Inspect service class {}", currentServiceClass.simpleName)
            var transactional = false
            var preAuthorize = false

            if (currentServiceClass.getAnnotation(Transactional::class.java) != null) {
                transactional = true
            }

            if (currentServiceClass.getAnnotation(PreAuthorize::class.java) != null) {
                preAuthorize = true
            }

            val methods = currentServiceClass.declaredMethods

            if (methods != null) {
                for (mthd in methods) {
                    if (mthd.getAnnotation(Transactional::class.java) != null) {
                        transactional = true
                    }

                    if (mthd.getAnnotation(PreAuthorize::class.java) != null) {
                        preAuthorize = true
                    }
                }
            }

            if (transactional) {
                LOGGER.error("ERROR: Bean {} is not a proxy but is Transactional! Class: {}.", name, currentServiceClass.name)
                inspectionSuccessful = false
            }

            if (preAuthorize) {
                LOGGER.error("ERROR: Bean {} is not a proxy but is PreAuthorize! Class: {}.", name, currentServiceClass.name)
                inspectionSuccessful = false
            }

            currentServiceClass = currentServiceClass.superclass
        } while (currentServiceClass != Any::class.java)

        return inspectionSuccessful
    }

    private fun isService(serviceCandidate: Any): Boolean {
        val serviceAnnotation = serviceCandidate.javaClass.getAnnotation(Service::class.java)
        val componentAnnotation = serviceCandidate.javaClass.getAnnotation(Component::class.java)

        return serviceAnnotation != null || componentAnnotation != null
    }
}