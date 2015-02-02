package com.amcbridge.jenkins.plugins.vsc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;

public class SvnManager implements VersionControlSystem
{

	private static SVNCommitInfo coomitFile(SVNNodeKind nodeKind,
			ISVNEditor editor, String filePath, byte[] data) throws SVNException
	{
		String checksum = BuildConfigurationManager.STRING_EMPTY;
		try 
		{
			editor.openRoot(-1);

			if (nodeKind == SVNNodeKind.FILE)
			{
				editor.openFile(filePath, -1);
			}
			else
			{
				editor.addFile(filePath, null, -1);
			}

			editor.applyTextDelta(filePath, null);
			SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();

			checksum = deltaGenerator.sendDelta(filePath,
					new ByteArrayInputStream(data), editor, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		editor.closeFile(filePath, checksum);
		editor.closeDir();
		return editor.closeEdit();
	}

	@SuppressWarnings("finally")
	public VersionControlSystemResult doCommit(String filePath, String url, String login,
			String password, String commitMessage)
	{
		SVNCommitInfo commitInfo = new SVNCommitInfo(0, login, null);
		VersionControlSystemResult commitResult = new VersionControlSystemResult(false);
		try
		{
			SVNRepositoryFactoryImpl.setup();

			SVNURL svnUrl = SVNURL.parseURIEncoded(url);
			SVNRepository repository = SVNRepositoryFactory.create(svnUrl);
			ISVNAuthenticationManager authManager = SVNWCUtil
					.createDefaultAuthenticationManager(login, password);
			repository.setAuthenticationManager(authManager);			
			byte[] fileBytesArray = Files.readAllBytes(Paths.get(filePath));
			SVNNodeKind nodeKind = repository.checkPath("config.xml", repository.getLatestRevision());
			ISVNEditor editor = repository.getCommitEditor(commitMessage , null );
			commitInfo = coomitFile(nodeKind, editor,
					BuildConfigurationManager.CONFIG_FILE_NAME, fileBytesArray);
			repository.closeSession();
			commitResult.setSuccess(true);
		}
		catch(Exception ex)
		{
			commitResult.setErrorMassage(ex.getMessage());
		}
		finally 
		{
			commitResult.setNumberOfRevision(commitInfo.getNewRevision());
			if(commitInfo.getDate() == null)
			{
				commitResult.setSuccess(false);
				commitResult.setErrorMassage(CommitError.FAIL.toString());
			}
			return commitResult;
		}
	}
}
