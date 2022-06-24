# onCreate
App README Template

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description

An app to help brainstorm ideas and to create a social media platform for users to share their ideas. The app requires a user to login using their email, and perform brainstorming exercises, such as Duolingo for a short amount of time each day. Using the app, the user will create a list of ideas for a project, company, app, etc. Using this list, users will be able to rank their ideas by tags or number rankings, and the top rated idea by the end of the day or week will automatically get exported into a standardized Google document. Furthermore, users will have the option to publically post their top rated ideas in a global page that other users can up/downvote to use for inspiration.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Productivity / Organization
- **Mobile:** Native Android app that allows students to brainstorm their ideas, and rank and share them afterwards.
- **Story:** Many people have tons of good them down or do not have a good place to store these ideas. Furthermore, a social media for ideas can provide inspiration for others and could be a way to encourage and foster brainstorming.
- **Market:** College students, workers, and anyone looking to brainstorm ideas for a company or app.
- **Habit:** Students can brainstorm 5 minutes every day practicing brainstorming activities (such as Duolingo).
- **Scope:** First, we intend **onCreate** to provide a place for brainstorming for individual users. Moreover, this could be expanded to provide a larger space for brainstorming that many users can use to share and rank ideas anonymously.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Users can register for a new account
* User can login/logout
* User can create a profile w/ an profile picture and description
* Home page with all personal ideas of a user
    * User can star, rank, and delete current ideas
* Brainstorming Page
    * User can can add a new idea
* Profile page that shows profile image and description
* Navigation bar to transition between personal ideas, brainstorming, profile, etc..
* Exporting top-ranked ideas into a standardized Google document


**Optional Nice-to-have Stories**

* Filtering system for personal ideas
* Global idea page to share ideas anonymously
    * switching between top, hot, and recent ideas
    * Allows users to rank ideas (up/downvote ideas)
    * Allows users to create a new idea post
* Brainstorming activities
    * Duolingo esc
    * ex: create as many ideas in 30 seconds

### 2. Screen Archetypes

* Login Screen
    * User can login
* Registration Screen
    * User can create a new account and add their school email
* Create Profile Screen
    * User can create a new profile
* Idea Screen
    * Home page
* Brainstorming Screen
    * User can create new ideas
    * (stretch) Users can complete brainstorming exercises to create ideas
* Profile Screen
    * Displays users profile picture and description
* Global Screen
    * Small tab bar to switch ideas presented in feed: top, hot, recent
    * Users can upvote/downvote public ideas
    * User can add a new idea publically

### 3. Navigation

**Tab Navigation** (Tab to Screen)

*Once logged in*
* Ideas -> Profile -> Brainstorm -> Global

*On Global screen*
* Idea viewing
    * Top -> Hot -> Recent


**Flow Navigation** (Screen to Screen)
* Login Screen
    * -> home/idea
    * -> registration
* Registration Screen
    * -> create profile
* Create Profile Screen
    * -> home/idea
* Idea Screen
    * can navigate through tabs between:
        * Idea Screen
        * Brainstorming Screen
        * Profile Screen
        * Global Screen

## Wireframes
<!-- <img src="![](https://i.imgur.com/8XM5cq4.jpg)
" width=600> -->
![](https://i.imgur.com/Vhg5Uj0.jpg)


<!-- ### [BONUS] Digital Wireframes & Mockups
### [BONUS] Interactive Prototype -->

## Schema

### Models
User

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for the user idea (default field) | 
| username      | String   | user's name |
| password      | String   | user's password for account |
| email         | String   | user's email for account |
| name          | String   | user's name |
| description   | String   | short description for the user |
| profileImage  | File     | image that the user has as their profile picture |
| ideaCount     | Number   | number of ideas a user has on their account |
| starIdeaCount | Number   | number of starred ideas a user has on their account |
| karma         | Number   | user's upvote score for posting on global section |
| createdAt     | DateTime | date when user account is created (default field) |
| updatedAt     | DateTime | date when user account is last updated (default field) |

PrivateIdea

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for the user idea (default field) | 
| author        | Pointer to User| image author |
| image         | File     | image that user has to accompany an idea |
| title         | String   | idea title by author |
| description   | String   | idea description body by author |
| starred       | Boolean  | describing whether an idea is starred or not by the user |
| createdAt     | DateTime | date when idea is created (default field) |
| updatedAt     | DateTime | date when idea is last updated (default field) |

GlobalIdea

| Property | Type | Description |
| -------- | -------- | -------- |
| objectId | String | unique id for the user idea (default field) | 
| author        | Pointer to User| image author |
| image         | File     | image that user has to accompany an idea |
| title         | String   | idea title by author |
| description   | String   | idea description body by author |
| starred       | Boolean  | describing whether an idea is starred or not by the user |
| createdAt     | DateTime | date when idea is created (default field) |
| updatedAt     | DateTime | date when idea is last updated (default field) |
| upvotes       | Number   | the amount of upvotes a global idea has |
| downvotes     | Number   | the amount of downvotes a global idea has |

### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]

* Login Screen
    * (Read/GET) get a user with inputed password/username
* Registration Screen
    * (Create/POST) create a new user
* Create Profile Screen
    * (Update/PUT) update the previously created user's profile screen
* Idea Screen
    * (Read/GET) query for all ideas where the user is author
    * (Update/PUT) star an existing idea
    * (Update/PUT) update the information on an existing idea
    * (Delete) delete an existing idea
* Brainstorming Screen
    * (Create/POST) create a new post
* Profile Screen
    * (Read/GET) query for user's profile picture, description, and name for their profile screen
* Global Screen
    * (Read/GET) query for all ideas in the database
    * (Update/PUT) up/downvote an existing
    * (Update/PUT) update the information on an existing user's idea
    * (Delete) delete an public idea

## Gifs

### Week 1:
<img src='https://github.com/Kiopsy/onCreate/blob/master/onCreateWeek1.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />
