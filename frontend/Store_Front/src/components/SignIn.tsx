import { Box, Button, Card, Center, Input, Stack } from "@chakra-ui/react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { Field } from "../components/ui/field";
import PostSender from "./RESTFul/PostSender";
import { UserEndPoint } from "./services/EndPoints";
import { PasswordInput } from "./ui/password-input";

const schema = z.object({
  EmailAddress: z.string().email({ message: "Please enter a valid email." }),
  Password: z.string().min(1, { message: "please input password." }),
});
const ENDPOINT = UserEndPoint;

type FormData = z.infer<typeof schema>;

const SignIn = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const [status, setStatus] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  //这个email是用来跳转页面用的
  const [email, setEmail] = useState("");

  //navigate
  const navigate = useNavigate();

  //用useEffect来检测登录状态登录成功时跳转。
  useEffect(() => {
    if (status === 200) navigate(`/mainpage/${email}`);
    if (!message) console.log(message);
    if (!error) console.log(error);
  }, [status, message, error]);

  const onSubmit = (data: FormData) => {
    console.log(data);
    PostSender(ENDPOINT, data, setStatus, setMessage, setError);
    setEmail(data.EmailAddress);
    //这个跳转只是用来测试的
    // navigate(`/mainpage/${data.EmailAddress}`);
  };

  return (
    <Box height="100vh" width="100vw">
      <Center>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Card.Root maxW="sm">
            <Card.Header>
              <Card.Title>STORE</Card.Title>
            </Card.Header>
            <Card.Body>
              <Stack gap="4" w="full">
                <Field label="Email" invalid={!!errors.EmailAddress}>
                  <Input
                    {...register("EmailAddress", {
                      required: "email is required",
                    })}
                    id="email"
                    type="email"
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.EmailAddress?.message}
                  </Box>
                </Field>

                <Field label="Password" invalid={!!errors.Password}>
                  <PasswordInput
                    {...register("Password", {
                      required: "password is required",
                    })}
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.Password?.message}
                  </Box>
                </Field>
              </Stack>
            </Card.Body>
            <Card.Footer justifyContent="flex-end">
              <Button
                color="black"
                onClick={() => {
                  navigate("/signup");
                }}
              >
                Sign Up
              </Button>
              <Button type="submit" color="black">
                Sign In
              </Button>
            </Card.Footer>
          </Card.Root>
        </form>
      </Center>
    </Box>
  );
};

export default SignIn;
