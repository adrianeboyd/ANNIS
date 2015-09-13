/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.admin.controller;

import annis.gui.admin.model.UserManagement;
import annis.gui.admin.view.UIView;
import annis.gui.admin.view.UserListView;
import annis.security.User;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.FutureCallback;
import com.sun.jersey.api.client.WebResource;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserController
  implements UserListView.Listener, UIView.Listener
{

  private final UserManagement model;

  private final UserListView view;

  private final UIView uiView;

  private boolean isLoggedIn = false;

  public UserController(UserManagement model,
    UserListView view, UIView uiView, boolean isLoggedIn)
  {
    this.model = model;
    this.view = view;
    this.uiView = uiView;
    this.isLoggedIn = isLoggedIn;
    view.addListener(UserController.this);
    uiView.addListener(UserController.this);
  }

  private void clearModel()
  {
    model.clear();
    view.setUserList(model.getUsers());
  }

  private void fetchFromService()
  {
    view.setLoadingAnimation(true);
    uiView.runInBackground(new Callable<Boolean>()
    {

      @Override
      public Boolean call() throws Exception
      {
        return model.fetchFromService();
      }
    }, new FutureCallback<Boolean>()
    {
      @Override
      public void onSuccess(Boolean result)
      {
        view.setLoadingAnimation(false);
        view.setUserList(model.getUsers());
      }

      @Override
      public void onFailure(Throwable t)
      {
        view.setLoadingAnimation(false);
        uiView.showWarning("Cannot get the user list", t.getMessage());
        view.setUserList(new LinkedList<User>());
      }
    });
  }

  @Override
  public void userUpdated(User user)
  {
    model.createOrUpdateUser(user);
  }

  @Override
  public void passwordChanged(String userName, String newPassword)
  {
    model.setPassword(userName, newPassword);
    view.setUserList(model.getUsers());
  }

  @Override
  public void addNewUser(String userName)
  {
    if (userName == null || userName.isEmpty())
    {
      uiView.showError("User name is empty", null);
    }
    else if (model.getUser(userName) != null)
    {
      uiView.showError("User already exists", null);
    }
    else
    {
      // create new user with empty password
      User u = new User(userName);
      if (model.createOrUpdateUser(u))
      {
        view.askForPasswordChange(userName);
        view.setUserList(model.getUsers());
        view.emptyNewUserNameTextField();
      }
    }
  }

  @Override
  public void deleteUsers(Set<String> userName)
  {
    for (String u : userName)
    {
      model.deleteUser(u);
    }
    view.setUserList(model.getUsers());

    if (userName.size() == 1)
    {
      uiView.showInfo("User \"" + userName.iterator().next() + "\" was deleted",
        null);
    }
    else
    {
      uiView.showInfo("Deleted users: " + Joiner.on(", ").join(userName), null);
    }
  }

  @Override
  public void loginChanged(WebResource annisRootResource, boolean isLoggedIn)
  {
    this.isLoggedIn = isLoggedIn;
    model.setRootResource(annisRootResource);
    if (isLoggedIn)
    {
      fetchFromService();
    }
    else
    {
      clearModel();
    }
  }

  @Override
  public void selectedTabChanged(Object selectedTab)
  {
    if (isLoggedIn && selectedTab == view)
    {
      fetchFromService();
    }

  }

}
