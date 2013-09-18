/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.shelbie.gogo.internal;

import java.util.Hashtable;

import org.apache.felix.gogo.command.Basic;
import org.apache.felix.gogo.command.Files;
import org.apache.felix.gogo.command.Inspect;
import org.apache.felix.gogo.command.OBR;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Register {@literal felix} and {@literal obr} scoped commands.
 */
@Component
@Instantiate
public class GogoCommand {
    private final BundleContext bundleContext;

    private ServiceTracker<?, ?> m_tracker = null;

    public GogoCommand(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Validate
    public void start() {
        Hashtable<String, Object> props = new Hashtable<String, Object>();

        // Register basic OSGi related commands
        props.put("osgi.command.scope", "felix");
        props.put("osgi.command.function",
                new String[] {
                    "bundlelevel", "frameworklevel", "headers",
                    /*"help", */"install", "lb", "log", "refresh",
                    "resolve", "start", "stop", "uninstall", "update",
                    "which"
                });
        bundleContext.registerService(Basic.class.getName(), new Basic(bundleContext), props);

        // Register "inspect" command for R4.3+
        props.put("osgi.command.scope", "felix");
        props.put("osgi.command.function", new String[] {"inspect"});
        bundleContext.registerService(Inspect.class.getName(), new Inspect(bundleContext), props);

        // Register file system related commands
        props.put("osgi.command.scope", "felix");
        props.put("osgi.command.function", new String[] {"cd", "ls"});
        bundleContext.registerService(Files.class.getName(), new Files(bundleContext), props);

        // Register OBR command (will only be usable if RepositoryAdmin service is available)
        m_tracker = new ServiceTracker<Object, Object>(
                bundleContext,
                "org.apache.felix.bundlerepository.RepositoryAdmin",
                null
        );
        m_tracker.open();
        props.put("osgi.command.scope", "obr");
        props.put("osgi.command.function",
                new String[] {
                    "deploy", "info", "javadoc", "list", "repos", "source"
                });
        bundleContext.registerService(OBR.class.getName(), new OBR(bundleContext, m_tracker), props);
    }

    @Invalidate
    public void stop() {
        m_tracker.close();
    }
}
