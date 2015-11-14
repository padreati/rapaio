# Introduction

As far as I understand one can commit code for a github project in two ways.
The first one is to clone a project you like and the second one is to commit
directly into a project which either you own or you collaborate on.

Either way you have to get a link from github to the project.
In case of collaboration the direct link to rapaio
project is `https://github.com/padreati/rapaio.git`. For a cloned repository you would get a link
 like `https://github.com/your_user_name/rapaio`, where *your_user_name* is your used name (sic!).

## Install Intellij Idea

If you don't already have it, grab the "community edition" of IDEA
from [[http://www.jetbrains.com/idea/download/]]. Extract the IntelliJ distribution somewhere
convenient (e.g. under your home directory), and run `bin/idea.sh` to start the IDE.
You should then be presented with a series of step-by-step configuration dialog boxes where you
can enable or disable plugins. The less plugins you have activated, the more resource-efficient
IDEA will be. The following steps will give you a very minimal IDEA setup.
You can always enable more plugins later if you need them.

1. From the version control plugins you need only Git.
2. From "Other plugins" you need GitHub and JUnit. .

**Super tip:** In recent versions of Ubuntu, it is not straightforward in the default GUI to
manually add application launcher icons. IDEA has a menu item to automatically
create one at `Tools -> Create desktop entry`.

## Install JDK

This library requires jdk 1.8 or newer. For testing the project needs junit. Other dependencies
are not needed since the first of the only two principles which governs how this library is build
is **Write yourself any feature you need, do not rely on external libraries**. As a
consequence, you need only JDK at runtime, and for testing only junit. Any other thing is either
a leverage of jdk (logging, image manipulation, graphics, etc) or written in library itself.


##  Setup `rapaio` from GitHub

1. Create a directory which will hold `rapaio` modules and perhaps other modules.
Suppose this directory is `/local/workspace/` (but you can name it how you like it).

2. IDEA allows you to check out Git repositories from GitHub in two ways. Either you have already
open idea with a project and you setup a new project for `rapaio` from there (in which case use
menu  `VCS` -> `Checkout from Version Control` -> `GitHub`) or you did not yet setup and opened
a project yet (in which case from `Welcome to Intellij IDEA` dialog box,
you use `Checkout from Version Control` -> `GitHub`).

3. In both cases you the `Login to GitHub` dialog box will apppear wnd you'll have to
complete `Host`, `Auth type`: Password, `Login` (usually the email) and `Password`
(usually the password for github).

4. If you checked `Save password` than you are asked for a master password. This password is
used by IDE to encrypt its own repository of passwords.

5. After successful login, in the `Clone repository` dialog box you can select a repository,
one which you would like is `https://github.com/padreati/rapaio.git`. Select a `Parent directory`
to be something like `/local/workspace`, select `Directory Name` as `rapaio` and push `Clone`
button.

6. The IDE asks you if you want to create a project from sources. In my setup I select `No` since
I usually want `rapaio` as a module. After selecting `No`, you will be back in
the `Welcome to Intellij IDEA` screen.

7. Now it's time to create the project. Select `Create new project`. In the `New project` modal
dialog select `Empty Project` from the right panel and click `Next` and from the new screen select
as `Project location`: `/home/workspace`, and perhaps `Project Name`: `rapaio`, and push `Finish`.

8. IDE will create the new empty project for you (please remember that there are inside sources
for rapaio) and open it. Because it's empty two things happens: it will show some warnings
`Unregistered VCS root detected` which you should ignore it for now and it will show modal
dialog `Project Structure` since the project is empty. In the `Project Structure` add a new
module via `+` -> `New module`. In the `New module` screen a `Module SDK`, it has to be JDK 8 or
greater (if none exists, than selects `New` -> `JDK` -> select JDK root -> `OK`). Then
select `Next`. On the new modal dialog select a location for `Content root` to be
`/local/workspaces/rapaio`, the other fields could remain the same, and click `Finish`.

9. Now it's time to solve `VCS root configuration problems`. For that you will have to click
`Configure` link from notifications and from the modal dialog you have to solve 2 things:
remove project from VCS (select project and click `-` button) and register `rapaio` under
VCS (select `rapaio` module and click `+` button).

10. After all of this is done you can start to work. Final setups might need to change markings
with right click and `Mark directory as` for src, tst or whatever. In order to experiment, you
can create your own module, add `rapaio` as a module dependency and work with data.

***Final note:*** please check if the sources for the project are marked as JDK 8 or greater.
In case it is not properly set (compilation errors or similar), you would have to go to
`Project Struncture` -> `Modules` -> `rapaio` -> `Sources` and set the field `Language level`
to `8 - Lambdas, types annotations, etc`

