class GroupsController < ApplicationController
	#before_filter :signed_in_user, only: [:show, :new]
	before_action :authenticate_user!, only: [:show, :new]
	def index
		@groups = Group.all
	end
	
	def show
		@group = Group.find(params[:id])
		@messages = @group.messages.all
	end
	
	def new
		@group = Group.new
	end
	
	def create
		group_params = params.require(:group)
		@group = Group.new(group_params.permit(:name))
		if params[:public] == 'false'
			@group.public = false
			@group.password = group_params[:password]
		end
		if @group.save
			redirect_to group_path(@group)
		else
			render 'new'
		end
	end
end
