#!/bin/sh

# NOTE: stdout/stderr might/should be discarded to not leak sensitive information.

echo "Post-processing (API) documentation."

# GH_TOKEN won't be available for PRs from forks.
# (http://docs.travis-ci.com/user/pull-requests/#Security-Restrictions-when-testing-Pull-Requests).
if [ -z "GH_TOKEN" ]; then
  echo "No GH_TOKEN available. Skipping."
  exit
fi

# NOTE: DO NOT USE "set -x", or anything else that would reveal GH_TOKEN!
set -e
set +x

REPO_APIDOC="https://${GH_TOKEN}@github.com/CvO-Theory/apt-javadoc"
REPO_DIR="$PWD"

# Export these to not add "git config" calls to the long command.
export GIT_AUTHOR_NAME="ParSysRobot on Travis CI"
export GIT_AUTHOR_EMAIL="ParSysRobot@users.noreply.github.com"
export GIT_COMMITTER_NAME="$GIT_AUTHOR_NAME"
export GIT_COMMITTER_EMAIL="$GIT_AUTHOR_EMAIL"

git clone --depth 1 --quiet --branch gh-pages $REPO_APIDOC /tmp/gh-pages-apidoc
cd /tmp/gh-pages-apidoc

rsync -a --delete --exclude=.git "${REPO_DIR}/doc/javadoc/" .
cp "${REPO_DIR}/apt.jar" .
git add --all .

# Commit the relevant changes.
COMMIT_MSG="Update docs via Travis

Last commit message:
$(cd $REPO_DIR && git log -1 --pretty=format:%s)

Build URL: https://travis-ci.org/CvO-Theory/apt/builds/${TRAVIS_BUILD_ID}"
git commit -m "$COMMIT_MSG"

git push --quiet origin gh-pages

# vim: filetype=sh:expandtab:shiftwidth=4:tabstop=8:softtabstop=4:textwidth=80
