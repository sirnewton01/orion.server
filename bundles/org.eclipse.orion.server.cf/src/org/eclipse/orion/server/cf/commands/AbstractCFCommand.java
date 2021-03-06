/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.cf.commands;

import org.eclipse.core.runtime.*;
import org.eclipse.orion.server.cf.CFExtServiceHelper;
import org.eclipse.orion.server.cf.objects.Cloud;
import org.eclipse.orion.server.cf.objects.Target;
import org.eclipse.orion.server.core.ServerStatus;

public abstract class AbstractCFCommand implements ICFCommand {

	protected Target target;
	private Cloud cloud;
	private boolean wasRun = false;

	protected AbstractCFCommand(Target target) {
		this.target = target;
	}

	protected AbstractCFCommand(Cloud cloud) {
		this.cloud = cloud;
	}

	public Cloud getCloud() {
		if (target != null)
			return target.getCloud();
		return cloud;
	}

	public Target getTarget() {
		return target;
	}

	@Override
	public IStatus doIt() {
		IStatus status = validateParams();
		if (!status.isOK())
			return status;

		ServerStatus doItStatus = this._doIt();
		ServerStatus result = retryIfNeeded(doItStatus);
		wasRun = true;
		return result;
	}

	private ServerStatus retryIfNeeded(ServerStatus doItStatus) {
		CFExtServiceHelper helper = CFExtServiceHelper.getDefault();
		if (doItStatus.getHttpCode() == 401 && target.getCloud().getAccessToken() != null && helper != null && helper.getService() != null) {
			target.getCloud().setAccessToken(helper.getService().getToken(target.getCloud()));
			return _doIt();
		}
		return doItStatus;
	}

	protected abstract ServerStatus _doIt();

	protected void assertWasRun() {
		Assert.isTrue(wasRun);
	}

	protected IStatus validateParams() {
		return Status.OK_STATUS;
	}
}
